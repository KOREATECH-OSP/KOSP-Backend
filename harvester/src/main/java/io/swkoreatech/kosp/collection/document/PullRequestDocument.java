package io.swkoreatech.kosp.collection.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

/**
 * GitHub 풀 리퀘스트 데이터를 저장하는 MongoDB 문서.
 *
 * <p>사용자가 생성한 GitHub PR의 메타데이터, 코드 변경 통계,
 * 병합 상태 등을 저장한다. 사용자-저장소-PR번호 유니크 인덱스를 포함한다.
 */
@Getter
@Builder
@Document(collection = "github_pull_requests")
@CompoundIndex(name = "user_repo_idx", def = "{'userId': 1, 'repositoryName': 1}")
@CompoundIndex(name = "unique_pr_idx", def = "{'userId': 1, 'repositoryName': 1, 'prNumber': 1}", unique = true)
public class PullRequestDocument {

    @Id
    private String id;

    private Long userId;
    private Long prNumber;
    private String title;
    private String state;
    private String repositoryName;
    private String repositoryOwner;

    private Integer additions;
    private Integer deletions;
    private Integer changedFiles;
    private Integer commitsCount;
    private Integer repoStarCount;
    private Integer closedIssuesCount;

    private Boolean merged;
    private Boolean isCrossRepository;
    private Instant mergedAt;
    private Instant createdAt;
    private Instant closedAt;

    private Instant collectedAt;
}
