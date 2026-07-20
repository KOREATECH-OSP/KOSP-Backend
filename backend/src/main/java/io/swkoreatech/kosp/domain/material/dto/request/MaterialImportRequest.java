package io.swkoreatech.kosp.domain.material.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import io.swkoreatech.kosp.domain.material.model.MaterialSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 아우누리 과제/EL 자료 반자동 수집(import) 요청.
 *
 * <p>학교 인증은 서버가 다루지 않는다. 사용자의 브라우저 확장이 이미 로그인된 세션에서
 * 스크랩한 정규화 데이터만 이 요청으로 전달하며(아우누리 세션/비밀번호는 전달되지 않는다),
 * 서버는 {@code (userId, source, sourceExternalId)} 기준으로 upsert 한다.
 * 자동 수집분은 공개 기본값 PRIVATE 로 저장된다.</p>
 *
 * @param items 수집된 자료 목록 (최소 1건)
 */
public record MaterialImportRequest(
    @NotEmpty @Valid List<Item> items
) {

    /**
     * 수집된 개별 자료.
     *
     * @param sourceExternalId 아우누리 자료 고유키 (upsert 멱등 기준, 필수)
     * @param source           출처 (AUNURI_ASSIGNMENT / AUNURI_EL, 필수)
     * @param title            자료명 (필수)
     * @param subjectName      과목명
     * @param materialYear     연도
     * @param semester         학기 (예: "1", "2", "여름", "겨울" 또는 "1학기")
     * @param sourceUrl        아우누리 원본 링크
     * @param fileUrl          업로드/원본 파일 URL
     * @param originalFileName 원본 파일명
     * @param fileSize         파일 크기(byte)
     * @param contentType      MIME 타입
     * @param materialDate     원본 게시/제출일 (최신순 정렬 기준)
     */
    public record Item(
        @NotBlank String sourceExternalId,
        @NotNull MaterialSource source,
        @NotBlank String title,
        String subjectName,
        Integer materialYear,
        String semester,
        String sourceUrl,
        String fileUrl,
        String originalFileName,
        Long fileSize,
        String contentType,
        LocalDateTime materialDate
    ) {
    }
}
