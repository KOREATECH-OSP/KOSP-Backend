package io.swkoreatech.kosp.collection.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

/**
 * GitHub 이슈 데이터를 저장하는 MongoDB 문서.
 *
 * <p>사용자가 생성한 GitHub 이슈의 메타데이터를 저장하며,
 * 사용자-저장소 복합 인덱스와 사용자-저장소-이슈번호 유니크 인덱스를 포함한다.
 */
@Getter
@Builder
@Document(collection = "github_issues")
@CompoundIndex(name = "user_repo_idx", def = "{'userId': 1, 'repositoryName': 1}")
@CompoundIndex(name = "unique_issue_idx", def = "{'userId': 1, 'repositoryName': 1, 'issueNumber': 1}", unique = true)
public class IssueDocument {

    @Id
    private String id;

    private Long userId;
    private Long issueNumber;
    private String title;
    private String state;
    private String repositoryName;
    private String repositoryOwner;

    private Integer commentsCount;

    private Instant createdAt;
    private Instant closedAt;

    private Instant collectedAt;
}
