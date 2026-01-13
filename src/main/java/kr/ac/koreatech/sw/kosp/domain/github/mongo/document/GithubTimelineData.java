package kr.ac.koreatech.sw.kosp.domain.github.mongo.document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * GitHub Timeline 데이터 (특정 기간의 사용자 활동)
 * 
 * Reference: SKKU-OSP parse_user_update() (Line 98-239)
 * - Timeline에서 파싱한 Issue/PR 항목들
 * - 월별 커밋 개수 (외부 값)
 */
@Document(collection = "github_timeline_data")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GithubTimelineData {
    
    @Id
    private String id;
    
    /**
     * GitHub 사용자 ID
     */
    private String githubId;
    
    /**
     * 조회 시작 날짜
     */
    private LocalDate fromDate;
    
    /**
     * 조회 종료 날짜
     */
    private LocalDate toDate;
    
    /**
     * Timeline에서 파싱한 Issue 목록
     */
    private List<GithubTimelineIssue> issues;
    
    /**
     * Timeline에서 파싱한 PR 목록
     */
    private List<GithubTimelinePR> prs;
    
    /**
     * Timeline에 표시된 커밋 개수 (외부 값)
     * GitHub가 직접 계산한 값이므로 더 정확
     */
    private Integer commitsCount;
    
    /**
     * 수집 시각
     */
    private LocalDateTime collectedAt;
    
    /**
     * Timeline 데이터 생성
     */
    public static GithubTimelineData create(
        String githubId,
        LocalDate fromDate,
        LocalDate toDate,
        List<GithubTimelineIssue> issues,
        List<GithubTimelinePR> prs,
        Integer commitsCount
    ) {
        return GithubTimelineData.builder()
            .githubId(githubId)
            .fromDate(fromDate)
            .toDate(toDate)
            .issues(issues)
            .prs(prs)
            .commitsCount(commitsCount)
            .collectedAt(LocalDateTime.now())
            .build();
    }
}
