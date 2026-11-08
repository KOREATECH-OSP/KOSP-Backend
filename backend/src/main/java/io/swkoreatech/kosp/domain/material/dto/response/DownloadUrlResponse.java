package io.swkoreatech.kosp.domain.material.dto.response;

/**
 * 파일 다운로드 URL 응답.
 *
 * <p>브라우저에서 열지 않고 곧바로 내려받도록 서명된 presigned GET URL을 반환한다.</p>
 *
 * @param downloadUrl presigned GET URL (attachment 강제)
 */
public record DownloadUrlResponse(
    String downloadUrl
) {
}
