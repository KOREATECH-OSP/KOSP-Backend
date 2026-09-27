package io.swkoreatech.kosp.collection.document;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

/**
 * 사용자별 일별 GitHub 수집 결과 통계를 저장하는 MongoDB 문서.
 *
 * <p>배치 Job 완료 시마다 수집 결과(발견 레포 수, 신규 커밋 수 등)를 기록하며,
 * 증분 수집 대상 유저의 커밋 수집 이상 여부를 N일 연속으로 판단하는 데 사용된다.
 */
@Getter
@Builder
@Document(collection = "daily_collection_stats")
@CompoundIndex(name = "unique_user_date_idx", def = "{'userId': 1, 'collectionDate': 1}", unique = true)
public class DailyCollectionStatDocument {

    @Id
    private String id;

    private Long userId;
    private LocalDate collectionDate;

    private int discoveredRepoCount;
    private int newCommitCount;
    private long batchDurationMs;

    private boolean incrementalCollection;

    private Instant createdAt;
}
