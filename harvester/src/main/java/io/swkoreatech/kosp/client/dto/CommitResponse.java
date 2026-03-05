package io.swkoreatech.kosp.client.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

/**
 * GitHub REST API의 커밋 상세 응답 DTO.
 *
 * <p>SHA, 커밋 메시지, 작성자 정보, 코드 변경 통계(추가/삭제/변경 파일 수)를 포함한다.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommitResponse {

    private String sha;
    private CommitInfo commit;
    private Stats stats;

    /**
     * 커밋 메시지를 반환한다.
     *
     * @return 커밋 메시지, 정보 없으면 null
     */
    public String getMessage() {
        if (commit == null) {
            return null;
        }
        return commit.getMessage();
    }

    /**
     * 작성자 이름을 반환한다.
     *
     * @return 작성자 이름, 정보 없으면 null
     */
    public String getAuthorName() {
        if (commit == null || commit.getAuthor() == null) {
            return null;
        }
        return commit.getAuthor().getName();
    }

    /**
     * 작성자 이메일을 반환한다.
     *
     * @return 작성자 이메일, 정보 없으면 null
     */
    public String getAuthorEmail() {
        if (commit == null || commit.getAuthor() == null) {
            return null;
        }
        return commit.getAuthor().getEmail();
    }

    /**
     * 작성 시각을 반환한다.
     *
     * @return 작성 시각, 정보 없으면 null
     */
    public Instant getAuthoredAt() {
        if (commit == null || commit.getAuthor() == null) {
            return null;
        }
        return commit.getAuthor().getDate();
    }

    /**
     * 추가된 라인 수를 반환한다.
     *
     * @return 추가 라인 수, 통계 없으면 0
     */
    public int getAdditions() {
        if (stats == null) {
            return 0;
        }
        return stats.getAdditions();
    }

    /**
     * 삭제된 라인 수를 반환한다.
     *
     * @return 삭제 라인 수, 통계 없으면 0
     */
    public int getDeletions() {
        if (stats == null) {
            return 0;
        }
        return stats.getDeletions();
    }

    /**
     * 변경된 파일 수를 반환한다.
     *
     * @return 변경 파일 수, 통계 없으면 0
     */
    public int getChangedFiles() {
        if (stats == null) {
            return 0;
        }
        return stats.getTotal();
    }

    /** 커밋 메시지와 작성자 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitInfo {
        private String message;
        private Author author;
    }

    /** 커밋 작성자 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String name;
        private String email;
        private Instant date;
    }

    /** 커밋의 코드 변경 통계를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stats {
        private int additions;
        private int deletions;
        private int total;
    }
}
