package io.swkoreatech.kosp.domain.resume.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swkoreatech.kosp.domain.resume.dto.request.ResumeSaveRequest;

/**
 * 프론트엔드 ResumePageClient.handleSave 가 실제로 전송하는 payload 를
 * ResumeSaveRequest 로 역직렬화할 수 있는지 검증한다.
 *
 * <p>프론트는 projects[].techStack 을 문자열 배열로 전송한다
 * (handleSave 의 apiProjects 변환). 백엔드 DTO 가 이를 받지 못하면
 * 400 이 발생하여 "저장해도 서버에 반영되지 않는" 증상이 된다.</p>
 */
class ResumeSaveRequestDeserializeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("프론트가 보내는 projects[].techStack 배열 payload 를 역직렬화할 수 있다")
    void deserializeFrontendPayloadWithTechStackArray() throws Exception {
        String frontendPayload = """
            {
              "resumeTitle": "내 이력서",
              "headline": "백엔드 개발자",
              "bio": "소개",
              "jobRole": "백엔드 개발자",
              "techStack": ["Java", "Spring"],
              "links": [],
              "education": [],
              "career": [],
              "experience": [],
              "projects": [
                {
                  "id": "abc1234",
                  "name": "KOSP",
                  "period": "2026.02.02 ~ 2026.08.02",
                  "summary": "요약",
                  "role": "백엔드",
                  "techStack": ["Java", "Spring Boot"],
                  "mainFeatures": "기능",
                  "myContributions": "기여",
                  "problemSolving": "문제해결",
                  "result": "결과",
                  "githubLink": "",
                  "deployLink": "",
                  "docLink": "",
                  "featured": "true"
                }
              ],
              "awards": [],
              "certifications": [],
              "coverLetters": [],
              "customSections": [],
              "isPublic": false,
              "visibleSections": {"sec-basic": true}
            }
            """;

        assertThatCode(() -> objectMapper.readValue(frontendPayload, ResumeSaveRequest.class))
            .doesNotThrowAnyException();

        ResumeSaveRequest parsed = objectMapper.readValue(frontendPayload, ResumeSaveRequest.class);
        assertThat(parsed.projects()).hasSize(1);
        assertThat(parsed.projects().get(0).techStack()).containsExactly("Java", "Spring Boot");
    }

    @Test
    @DisplayName("과거 포맷(콤마 구분 단일 문자열) techStack 도 배열로 정규화한다")
    void deserializeLegacyCommaSeparatedTechStack() throws Exception {
        String legacyPayload = """
            {
              "projects": [
                {"id": "old1", "name": "레거시", "techStack": "Java, Spring Boot"}
              ]
            }
            """;

        ResumeSaveRequest parsed = objectMapper.readValue(legacyPayload, ResumeSaveRequest.class);

        assertThat(parsed.projects().get(0).techStack()).containsExactly("Java", "Spring Boot");
    }

    @Test
    @DisplayName("techStack 이 null 이거나 빈 배열이어도 실패하지 않는다")
    void deserializeNullOrEmptyTechStack() throws Exception {
        String payload = """
            {
              "projects": [
                {"id": "a", "name": "널", "techStack": null},
                {"id": "b", "name": "빈배열", "techStack": []},
                {"id": "c", "name": "빈문자열", "techStack": ""}
              ]
            }
            """;

        ResumeSaveRequest parsed = objectMapper.readValue(payload, ResumeSaveRequest.class);

        assertThat(parsed.projects()).hasSize(3);
        assertThat(parsed.projects()).allSatisfy(p -> assertThat(p.techStack()).isEmpty());
    }
}
