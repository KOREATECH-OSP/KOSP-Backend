package kr.ac.koreatech.sw.kosp.domain.github.e2e;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.*;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubDataCollectionService;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubTimelineScrapingService;
import reactor.test.StepVerifier;

/**
 * E2E tests for complete user collection pipeline
 * 
 * Requires GITHUB_TOKEN environment variable
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Complete Collection E2E 테스트")
@EnabledIfEnvironmentVariable(named = "GITHUB_TOKEN", matches = ".+")
class CompleteCollectionE2ETest {
    
    @Autowired
    private GithubDataCollectionService dataCollectionService;
    
    @Autowired
    private GithubTimelineScrapingService timelineScrapingService;
    
    @Autowired
    private GithubUserBasicRawRepository userBasicRawRepository;
    
    @Autowired
    private GithubUserFollowingRepository userFollowingRepository;
    
    @Autowired
    private GithubUserStarredRepository userStarredRepository;
    
    @Autowired
    private GithubTimelineDataRepository timelineDataRepository;
    
    @Value("${GITHUB_TOKEN:}")
    private String githubToken;
    
    @Test
    @DisplayName("전체 사용자 수집: 모든 데이터 수집 및 검증")
    void completeUserCollection_shouldCollectAllData() {
        // Given
        String testUser = "octocat";
        
        // Clean up before test
        userBasicRawRepository.deleteAll();
        userFollowingRepository.deleteAll();
        userStarredRepository.deleteAll();
        timelineDataRepository.deleteAll();
        
        // When - Collect user basic info
        var userBasicResult = dataCollectionService.collectUserBasicInfo(testUser, githubToken);
        
        // Then - Verify user basic info
        StepVerifier.create(userBasicResult)
            .assertNext(userBasic -> {
                assertThat(userBasic).isNotNull();
                assertThat(userBasic.getGithubId()).isEqualTo(testUser);
                assertThat(userBasic.getTotalStars()).isNotNull();
                System.out.println("✅ User basic info collected");
                System.out.println("   Total Stars: " + userBasic.getTotalStars());
            })
            .verifyComplete();
        
        // When - Collect following
        var followingResult = dataCollectionService.collectFollowing(testUser, githubToken);
        
        // Then - Verify following
        StepVerifier.create(followingResult)
            .assertNext(following -> {
                assertThat(following).isNotNull();
                System.out.println("✅ Following collected: " + following.size());
            })
            .verifyComplete();
        
        // When - Collect starred
        var starredResult = dataCollectionService.collectStarred(testUser, githubToken);
        
        // Then - Verify starred
        StepVerifier.create(starredResult)
            .assertNext(starred -> {
                assertThat(starred).isNotNull();
                System.out.println("✅ Starred collected: " + starred.size());
            })
            .verifyComplete();
        
        // Verify data consistency
        long userBasicCount = userBasicRawRepository.count();
        long followingCount = userFollowingRepository.countByGithubId(testUser);
        long starredCount = userStarredRepository.countByGithubId(testUser);
        
        assertThat(userBasicCount).isEqualTo(1);
        System.out.println("✅ Complete collection verified");
        System.out.println("   Following: " + followingCount);
        System.out.println("   Starred: " + starredCount);
    }
}
