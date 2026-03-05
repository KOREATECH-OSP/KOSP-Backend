package io.swkoreatech.kosp.domain.community.recruit.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 모집 공고 작성/수정 요청 DTO.
 *
 * @param boardId 게시판 ID
 * @param title 제목
 * @param content 내용
 * @param tags 태그 목록
 * @param teamId 팀 ID
 * @param startDate 모집 시작일
 * @param endDate 모집 종료일 (선택)
 */
public record RecruitRequest(
    @NotNull(message = "게시판 ID는 필수입니다.")
    Long boardId,

    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @NotBlank(message = "내용은 필수입니다.")
    String content,

    List<String> tags,

    @NotNull(message = "팀 ID는 필수입니다.")
    Long teamId,

    @NotNull(message = "모집 시작일은 필수입니다.")
    LocalDateTime startDate,

    LocalDateTime endDate
) {
}
