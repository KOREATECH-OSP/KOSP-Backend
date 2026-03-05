package io.swkoreatech.kosp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

/**
 * GitHub REST API의 커밋 목록 항목 응답 DTO.
 *
 * <p>커밋 목록 조회 시 반환되는 개별 커밋의 SHA와 커밋 메시지 정보를 담는다.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommitListItem {

    private String sha;
    private CommitInfo commit;

    /**
     * 커밋 메시지를 반환한다.
     *
     * @return 커밋 메시지, 커밋 정보가 없으면 null
     */
    public String getMessage() {
        if (commit == null) {
            return null;
        }
        return commit.getMessage();
    }

    /**
     * 커밋의 기본 정보를 담는 내부 DTO.
     */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitInfo {
        private String message;
    }
}
