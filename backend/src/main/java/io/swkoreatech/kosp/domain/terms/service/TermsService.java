package io.swkoreatech.kosp.domain.terms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.terms.model.Terms;
import io.swkoreatech.kosp.common.terms.model.UserTermsAgreement;
import io.swkoreatech.kosp.common.terms.repository.TermsRepository;
import io.swkoreatech.kosp.common.terms.repository.UserTermsAgreementRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.terms.dto.request.AgreeTermsRequest;
import io.swkoreatech.kosp.domain.terms.dto.response.TermsResponse;
import lombok.RequiredArgsConstructor;

/**
 * 약관 서비스.
 * <p>약관 조회, 사용자 동의 저장, 미동의 여부 판단을 처리한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

    private final TermsRepository termsRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;
    private final UserRepository userRepository;

    /**
     * 현재 유효한 서비스 이용약관(SERVICE)을 조회한다.
     *
     * @return 현재 약관 응답
     */
    public TermsResponse getActiveTerms() {
        Terms terms = termsRepository.getActive();
        return TermsResponse.from(terms);
    }

    /**
     * 사용자가 지정한 버전의 약관에 동의를 저장한다.
     * 이미 동의한 경우 멱등하게 처리한다 (중복 저장 없음).
     *
     * @param userId  사용자 ID
     * @param request 동의 요청 (약관 버전 포함)
     */
    @Transactional
    public void agreeTerms(Long userId, AgreeTermsRequest request) {
        User user = userRepository.getById(userId);
        Terms terms = termsRepository.getByVersion(request.version());
        saveAgreement(user, terms);
    }

    /**
     * 사용자가 현재 활성화된 약관(종류별 1개씩) 중 하나라도 동의하지 않았는지 판단한다.
     * 활성 약관이 하나도 없으면 동의가 필요 없다.
     *
     * @param user 대상 사용자
     * @return 미동의 약관이 하나라도 있으면 {@code true}
     */
    public boolean needsTermsAgreement(User user) {
        return termsRepository.findAllByIsActiveTrue().stream()
            .anyMatch(terms -> !userTermsAgreementRepository.existsByUserAndTerms(user, terms));
    }

    /**
     * 회원가입 시 약관 버전이 제공된 경우 동의를 저장한다.
     * 버전이 null이거나 빈 문자열이거나 존재하지 않는 버전이면 저장하지 않는다.
     * 같은 버전 문자열이 여러 종류에 있어 약관을 특정할 수 없을 때도 저장하지 않는다.
     *
     * @param user         가입 완료된 사용자
     * @param termsVersion 동의한 약관 버전 (nullable)
     */
    @Transactional
    public void agreeTermsIfProvided(User user, String termsVersion) {
        if (termsVersion == null || termsVersion.isBlank()) {
            return;
        }

        List<Terms> matched = termsRepository.findByVersion(termsVersion);
        if (matched.size() != 1) {
            return;
        }
        saveAgreement(user, matched.get(0));
    }

    private void saveAgreement(User user, Terms terms) {
        if (userTermsAgreementRepository.existsByUserAndTerms(user, terms)) {
            return;
        }
        userTermsAgreementRepository.save(UserTermsAgreement.builder()
            .user(user)
            .terms(terms)
            .agreedAt(LocalDateTime.now())
            .build());
    }
}
