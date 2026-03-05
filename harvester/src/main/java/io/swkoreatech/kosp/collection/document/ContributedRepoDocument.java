package io.swkoreatech.kosp.collection.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

/**
 * 사용자가 기여한 GitHub 저장소 데이터를 저장하는 MongoDB 문서.
 *
 * <p>저장소 기본 정보(이름, 소유자, 스타 수 등)와 사용자의 기여 통계
 * (커밋 수, PR 수, 이슈 수)를 함께 저장한다.
 */
@Getter
@Builder
@Document(collection = "github_contributed_repos")
public class ContributedRepoDocument {

    @Id
    private String id;

    @Indexed
    private Long userId;

    private String repositoryName;
    private String repositoryOwner;
    private String fullName;
    private String description;

    private Boolean isOwner;
    private Boolean isFork;
    private Boolean isPrivate;

    private String primaryLanguage;
    private Integer stargazersCount;
    private Integer forksCount;
    private Integer watchersCount;
    private Instant repoCreatedAt;

    private Integer userCommitCount;
    private Integer userPrCount;
    private Integer userIssueCount;

    private Instant lastContributedAt;
    private Instant collectedAt;

    /**
     * 사용자의 저장소별 기여 통계를 업데이트한다.
     *
     * @param commitCount     커밋 수
     * @param prCount         풀 리퀘스트 수
     * @param issueCount      이슈 수
     * @param lastContributed 마지막 기여 시각
     */
    public void updateUserStats(int commitCount, int prCount, int issueCount, Instant lastContributed) {
        this.userCommitCount = commitCount;
        this.userPrCount = prCount;
        this.userIssueCount = issueCount;
        this.lastContributedAt = lastContributed;
    }

    /**
     * 저장소의 전체 이름(owner/name)을 반환한다.
     *
     * @return "소유자/저장소명" 형식의 문자열
     */
    public String getRepoFullName() {
        return repositoryOwner + "/" + repositoryName;
    }
}
