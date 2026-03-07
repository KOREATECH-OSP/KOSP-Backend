package io.swkoreatech.kosp.client.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

/**
 * GitHub GraphQL API의 저장소별 커밋 목록 응답 DTO.
 *
 * <p>저장소의 기본 브랜치에서 특정 작성자의 커밋 이력을 담으며,
 * 커서 기반 페이지네이션을 지원한다.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryCommitsResponse {

    private Repository repository;

    /**
     * 커밋 노드 목록을 반환한다.
     *
     * @return 커밋 노드 목록, 데이터 없으면 빈 목록
     */
    public List<CommitNode> getCommits() {
        if (repository == null || repository.getDefaultBranchRef() == null) {
            return Collections.emptyList();
        }
        var target = repository.getDefaultBranchRef().getTarget();
        if (target == null || target.getHistory() == null) {
            return Collections.emptyList();
        }
        return target.getHistory().getNodes();
    }

    /**
     * 페이지네이션 정보를 반환한다.
     *
     * @return 페이지 정보, 데이터 없으면 null
     */
    public PageInfo getPageInfo() {
        if (repository == null || repository.getDefaultBranchRef() == null) {
            return null;
        }
        var target = repository.getDefaultBranchRef().getTarget();
        if (target == null || target.getHistory() == null) {
            return null;
        }
        return target.getHistory().getPageInfo();
    }

    /** 저장소 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        private DefaultBranchRef defaultBranchRef;
    }

    /** 기본 브랜치 참조 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DefaultBranchRef {
        private Target target;
    }

    /** Git 타겟(커밋 히스토리) 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Target {
        private History history;
    }

    /** 커밋 히스토리(노드 목록 + 페이지 정보)를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class History {
        private PageInfo pageInfo;
        private List<CommitNode> nodes;
    }

    /** 페이지네이션 메타데이터를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageInfo {
        private boolean hasNextPage;
        private String endCursor;
    }

    /** 개별 커밋 노드의 상세 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitNode {
        private String oid;
        private String message;
        private int additions;
        private int deletions;
        private Integer changedFilesIfAvailable;
        private Instant authoredDate;
        private Author author;

        /**
         * 작성자 이름을 반환한다.
         *
         * @return 작성자 이름, 정보 없으면 null
         */
        public String getAuthorName() {
            return author != null ? author.getName() : null;
        }

        /**
         * 작성자 이메일을 반환한다.
         *
         * @return 작성자 이메일, 정보 없으면 null
         */
        public String getAuthorEmail() {
            return author != null ? author.getEmail() : null;
        }

        /**
         * 변경된 파일 수를 반환한다.
         *
         * @return 변경 파일 수, 정보 없으면 0
         */
        public int getChangedFiles() {
            return changedFilesIfAvailable != null ? changedFilesIfAvailable : 0;
        }
    }

    /** 커밋 작성자 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String name;
        private String email;
    }
}
