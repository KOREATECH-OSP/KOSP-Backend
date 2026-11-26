package io.swkoreatech.kosp.domain.resume.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.resume.export.ResumeHwpxExporter;
import io.swkoreatech.kosp.domain.resume.model.UserResume;
import io.swkoreatech.kosp.domain.resume.repository.UserResumeRepository;
import io.swkoreatech.kosp.global.common.fixture.TestUserFixture;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeExportService - hwpx 내려받기 권한")
class ResumeExportServiceTest {

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_ID = 2L;
    private static final Long RESUME_ID = 100L;

    @Mock
    private UserResumeRepository userResumeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ResumeExportService service;

    private ResumeExportService service() {
        if (service == null) {
            service = new ResumeExportService(
                userResumeRepository, new ResumeHwpxExporter(objectMapper), objectMapper);
        }
        return service;
    }

    private UserResume resume(String json) {
        UserResume r = UserResume.builder().user(null).resumeData(json).isDefault(true).build();
        ReflectionTestUtils.setField(r, "id", RESUME_ID);
        return r;
    }

    private User owner() {
        return TestUserFixture.createUser(OWNER_ID);
    }

    private static final String PUBLIC_JSON = "{\"resumeTitle\":\"공개 이력서\",\"isPublic\":true}";
    private static final String PRIVATE_JSON = "{\"resumeTitle\":\"비공개 이력서\",\"isPublic\":false}";

    @Nested
    @DisplayName("본인 이력서")
    class Mine {

        @Test
        @DisplayName("본인 소유이면 생성된다")
        void ownerSucceeds() {
            given(userResumeRepository.findByIdAndUserId(RESUME_ID, OWNER_ID))
                .willReturn(Optional.of(resume(PRIVATE_JSON)));

            var document = service().exportMine(owner(), RESUME_ID);

            // 비공개여도 본인은 받을 수 있어야 한다
            assertThat(document.content()).isNotEmpty();
            assertThat(document.fileName()).isEqualTo("비공개 이력서.hwpx");
        }

        @Test
        @DisplayName("본인 소유가 아니면 404")
        void notOwnerIs404() {
            given(userResumeRepository.findByIdAndUserId(RESUME_ID, OWNER_ID))
                .willReturn(Optional.empty());

            User user = owner();
            assertThatThrownBy(() -> service().exportMine(user, RESUME_ID))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ExceptionMessage.NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("타인 이력서")
    class Others {

        @Test
        @DisplayName("공개 이력서는 생성된다")
        void publicSucceeds() {
            given(userResumeRepository.findByIdAndUserId(RESUME_ID, OTHER_ID))
                .willReturn(Optional.of(resume(PUBLIC_JSON)));

            var document = service().exportPublic(OTHER_ID, RESUME_ID);

            assertThat(document.content()).isNotEmpty();
            assertThat(document.fileName()).isEqualTo("공개 이력서.hwpx");
        }

        @Test
        @DisplayName("비공개 이력서는 404 (403 이 아니라 존재를 숨긴다)")
        void privateIs404() {
            given(userResumeRepository.findByIdAndUserId(RESUME_ID, OTHER_ID))
                .willReturn(Optional.of(resume(PRIVATE_JSON)));

            assertThatThrownBy(() -> service().exportPublic(OTHER_ID, RESUME_ID))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ExceptionMessage.NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("isPublic 필드가 아예 없으면 404")
        void missingIsPublicIs404() {
            given(userResumeRepository.findByIdAndUserId(RESUME_ID, OTHER_ID))
                .willReturn(Optional.of(resume("{\"resumeTitle\":\"제목\"}")));

            assertThatThrownBy(() -> service().exportPublic(OTHER_ID, RESUME_ID))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ExceptionMessage.NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("isPublic 이 문자열 \"true\" 여도 404 (불리언만 인정)")
        void stringTrueIs404() {
            given(userResumeRepository.findByIdAndUserId(RESUME_ID, OTHER_ID))
                .willReturn(Optional.of(resume("{\"isPublic\":\"true\"}")));

            assertThatThrownBy(() -> service().exportPublic(OTHER_ID, RESUME_ID))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ExceptionMessage.NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("resume_data 가 깨져 있으면 비공개로 보고 404")
        void brokenJsonIs404() {
            given(userResumeRepository.findByIdAndUserId(RESUME_ID, OTHER_ID))
                .willReturn(Optional.of(resume("{broken")));

            assertThatThrownBy(() -> service().exportPublic(OTHER_ID, RESUME_ID))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ExceptionMessage.NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("resume_data 가 null 이면 404")
        void nullDataIs404() {
            given(userResumeRepository.findByIdAndUserId(RESUME_ID, OTHER_ID))
                .willReturn(Optional.of(resume(null)));

            assertThatThrownBy(() -> service().exportPublic(OTHER_ID, RESUME_ID))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ExceptionMessage.NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("해당 소유자의 이력서가 아니면 404")
        void wrongOwnerIs404() {
            given(userResumeRepository.findByIdAndUserId(RESUME_ID, OTHER_ID))
                .willReturn(Optional.empty());

            assertThatThrownBy(() -> service().exportPublic(OTHER_ID, RESUME_ID))
                .isInstanceOf(GlobalException.class)
                .hasMessage(ExceptionMessage.NOT_FOUND.getMessage());
        }
    }
}
