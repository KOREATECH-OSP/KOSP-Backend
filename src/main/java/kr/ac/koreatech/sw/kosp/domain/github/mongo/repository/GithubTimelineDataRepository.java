package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubTimelineData;

/**
 * GitHub Timeline 데이터 Repository
 */
public interface GithubTimelineDataRepository extends MongoRepository<GithubTimelineData, String> {
    
    /**
     * 사용자의 모든 Timeline 데이터 조회
     */
    List<GithubTimelineData> findByGithubId(String githubId);
    
    /**
     * 사용자의 특정 기간 Timeline 데이터 조회
     */
    List<GithubTimelineData> findByGithubIdAndFromDateBetween(
        String githubId, 
        LocalDate startDate, 
        LocalDate endDate
    );
    
    /**
     * 특정 기간의 Timeline 데이터 존재 여부
     */
    boolean existsByGithubIdAndFromDateAndToDate(
        String githubId,
        LocalDate fromDate,
        LocalDate toDate
    );
}
