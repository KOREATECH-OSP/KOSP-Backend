package io.swkoreatech.kosp.domain.season.mongo;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;

/**
 * 시즌 커밋 점수 계산을 위한 GitHub 커밋 MongoDB 문서 읽기 전용 뷰.
 *
 * <p>harvester가 수집한 {@code github_commits} 컬렉션을 직접 읽는다.
 * 필드 이름은 harvester의 CommitDocument와 동일해야 한다.</p>
 */
@Getter
@Document(collection = "github_commits")
public class SeasonCommitDocument {

    @Id
    private String id;

    private Long userId;
    private String sha;
    private String message;
    private String repositoryOwner;
    private String repositoryName;
    private Instant authoredAt;
}
