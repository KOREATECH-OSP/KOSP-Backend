package kr.ac.koreatech.sw.kosp.domain.github.mongo.document;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Timeline에서 파싱한 PR 정보
 * 
 * Reference: SKKU-OSP PullRequest item (Line 150-165)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GithubTimelinePR {
    
    /**
     * PR 작성자 (Timeline 소유자)
     */
    private String githubId;
    
    /**
     * 저장소 소유자
     */
    private String ownerId;
    
    /**
     * 저장소 이름
     */
    private String repoName;
    
    /**
     * PR 제목
     */
    private String title;
    
    /**
     * PR 번호
     */
    private Integer number;
    
    /**
     * 생성 날짜
     */
    private LocalDate date;
    
    /**
     * 소유 저장소 여부 (ownerId == githubId)
     */
    private Boolean isOwnedRepo;
}
