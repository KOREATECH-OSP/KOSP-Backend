package io.swkoreatech.kosp.domain.terms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.terms.model.Terms;
import io.swkoreatech.kosp.common.terms.model.TermsType;
import io.swkoreatech.kosp.common.terms.model.UserTermsAgreement;
import io.swkoreatech.kosp.common.terms.repository.TermsRepository;
import io.swkoreatech.kosp.common.terms.repository.UserTermsAgreementRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.terms.dto.request.AgreeTermsRequest;
import io.swkoreatech.kosp.domain.terms.dto.response.TermsResponse;
import io.swkoreatech.kosp.global.common.IntegrationTestSupport;
import jakarta.persistence.EntityManager;

/**
 * 실제 JPA 레포지토리(H2)로 약관 서비스를 검증한다.
 *
 * <p>핵심: 종류가 다른 활성 약관이 동시에 여러 개 있어도 조회가
 * {@code IncorrectResultSizeDataAccessException} 없이 동작해야 한다.</p>
 */
class TermsServiceTest extends IntegrationTestSupport {

    @Autowired
    private TermsService termsService;

    @Autowired
    private TermsRepository termsRepository;

    @Autowired
    private UserTermsAgreementRepository agreementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User user;

    @BeforeEach
    void setUp() {
        createGithubUser(7001L);
        user = userRepository.save(User.builder()
            .name("약관테스트")
            .kutId("2026001")
            .kutEmail("terms@koreatech.ac.kr")
            .password(passwordEncoder.encode(getValidPassword()))
            .githubUser(githubUserRepository.getByGithubId(7001L))
            .build());
    }

    private Terms saveTerms(TermsType type, String version, boolean active) {
        Terms terms = Terms.builder()
            .type(type)
            .version(version)
            .content(type.getDescription() + " " + version)
            .isActive(active)
            .build();
        entityManager.persist(terms);
        entityManager.flush();
        return terms;
    }

    private void agree(Terms terms) {
        agreementRepository.save(UserTermsAgreement.builder()
            .user(user)
            .terms(terms)
            .agreedAt(LocalDateTime.now())
            .build());
    }

    @Nested
    @DisplayName("needsTermsAgreement 메서드")
    class NeedsTermsAgreementTest {

        @Test
        @DisplayName("SERVICE·PRIVACY 활성 약관이 둘이어도 예외 없이 동작한다")
        void twoActiveTypes_noException() {
            saveTerms(TermsType.SERVICE, "1.0", true);
            saveTerms(TermsType.PRIVACY, "1.0", true);

            assertThatCode(() -> termsService.needsTermsAgreement(user)).doesNotThrowAnyException();
            assertThat(termsRepository.findAllByIsActiveTrue()).hasSize(2);
        }

        @Test
        @DisplayName("둘 중 SERVICE 만 동의하면 true")
        void onlyServiceAgreed_true() {
            Terms service = saveTerms(TermsType.SERVICE, "1.0", true);
            saveTerms(TermsType.PRIVACY, "1.0", true);
            agree(service);

            assertThat(termsService.needsTermsAgreement(user)).isTrue();
        }

        @Test
        @DisplayName("둘 중 PRIVACY 만 동의하면 true")
        void onlyPrivacyAgreed_true() {
            saveTerms(TermsType.SERVICE, "1.0", true);
            Terms privacy = saveTerms(TermsType.PRIVACY, "1.0", true);
            agree(privacy);

            assertThat(termsService.needsTermsAgreement(user)).isTrue();
        }

        @Test
        @DisplayName("둘 다 동의하면 false")
        void bothAgreed_false() {
            agree(saveTerms(TermsType.SERVICE, "1.0", true));
            agree(saveTerms(TermsType.PRIVACY, "1.0", true));

            assertThat(termsService.needsTermsAgreement(user)).isFalse();
        }

        @Test
        @DisplayName("비활성 약관 미동의는 판단에 영향이 없다")
        void inactiveIgnored_false() {
            agree(saveTerms(TermsType.SERVICE, "2.0", true));
            saveTerms(TermsType.SERVICE, "1.0", false);

            assertThat(termsService.needsTermsAgreement(user)).isFalse();
        }

        @Test
        @DisplayName("활성 약관이 하나도 없으면 false (기존 동작 유지)")
        void noActive_false() {
            saveTerms(TermsType.SERVICE, "1.0", false);

            assertThat(termsService.needsTermsAgreement(user)).isFalse();
        }
    }

    @Nested
    @DisplayName("getActiveTerms 메서드")
    class GetActiveTermsTest {

        @Test
        @DisplayName("활성 약관이 둘이어도 SERVICE 를 반환한다")
        void returnsService() {
            Terms service = saveTerms(TermsType.SERVICE, "1.0", true);
            saveTerms(TermsType.PRIVACY, "1.1", true);

            TermsResponse response = termsService.getActiveTerms();

            assertThat(response.id()).isEqualTo(service.getId());
        }

        @Test
        @DisplayName("SERVICE 활성 약관이 없으면 TERMS_NOT_FOUND")
        void noService_throws() {
            saveTerms(TermsType.PRIVACY, "1.0", true);

            assertThatThrownBy(() -> termsService.getActiveTerms())
                .isInstanceOf(GlobalException.class)
                .hasMessage(ExceptionMessage.TERMS_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("agreeTerms 메서드")
    class AgreeTermsTest {

        @Test
        @DisplayName("버전이 유일하면 해당 약관에 동의가 저장되고, 반복 호출해도 멱등하다")
        void uniqueVersion_saved() {
            Terms service = saveTerms(TermsType.SERVICE, "1.0", true);

            termsService.agreeTerms(user.getId(), new AgreeTermsRequest("1.0"));
            termsService.agreeTerms(user.getId(), new AgreeTermsRequest("1.0"));

            assertThat(agreementRepository.existsByUserAndTerms(user, service)).isTrue();
        }

        @Test
        @DisplayName("같은 버전이 여러 종류에 있으면 TERMS_VERSION_AMBIGUOUS")
        void ambiguousVersion_throws() {
            saveTerms(TermsType.SERVICE, "1.0", true);
            saveTerms(TermsType.PRIVACY, "1.0", true);

            assertThatThrownBy(() -> termsService.agreeTerms(user.getId(), new AgreeTermsRequest("1.0")))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ExceptionMessage.TERMS_VERSION_AMBIGUOUS.getMessage());
        }

        @Test
        @DisplayName("없는 버전이면 TERMS_VERSION_NOT_FOUND")
        void missingVersion_throws() {
            assertThatThrownBy(() -> termsService.agreeTerms(user.getId(), new AgreeTermsRequest("9.9")))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ExceptionMessage.TERMS_VERSION_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("agreeTermsIfProvided 메서드")
    class AgreeTermsIfProvidedTest {

        @Test
        @DisplayName("버전이 유일하면 동의를 저장한다")
        void uniqueVersion_saved() {
            Terms service = saveTerms(TermsType.SERVICE, "1.0", true);

            termsService.agreeTermsIfProvided(user, "1.0");

            assertThat(agreementRepository.existsByUserAndTerms(user, service)).isTrue();
        }

        @Test
        @DisplayName("같은 버전이 여러 종류에 있으면 예외 없이 저장하지 않는다")
        void ambiguousVersion_skipped() {
            Terms service = saveTerms(TermsType.SERVICE, "1.0", true);
            Terms privacy = saveTerms(TermsType.PRIVACY, "1.0", true);

            assertThatCode(() -> termsService.agreeTermsIfProvided(user, "1.0")).doesNotThrowAnyException();
            assertThat(agreementRepository.existsByUserAndTerms(user, service)).isFalse();
            assertThat(agreementRepository.existsByUserAndTerms(user, privacy)).isFalse();
        }
    }
}
