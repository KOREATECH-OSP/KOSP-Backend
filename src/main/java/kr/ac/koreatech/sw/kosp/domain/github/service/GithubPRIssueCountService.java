package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubPRIssueCountService {

    private final MongoTemplate mongoTemplate;

    /**
     * 사용자가 생성한 PR 수 계산
     */
    public int countUserPRs(String githubLogin) {
        try {
            // MongoDB Aggregation Pipeline:
            // 1. $unwind: pullRequests 배열을 개별 문서로 분리
            // 2. $match: user.login이 githubLogin과 일치하는 것만 필터링
            // 3. $count: 개수 세기
            
            Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.unwind("pullRequests"),
                Aggregation.match(Criteria.where("pullRequests.user.login").is(githubLogin)),
                Aggregation.count().as("count")
            );

            AggregationResults<CountResult> results = mongoTemplate.aggregate(
                aggregation,
                "github_prs_raw",
                CountResult.class
            );

            List<CountResult> resultList = results.getMappedResults();
            
            if (resultList.isEmpty()) {
                return 0;
            }

            int count = resultList.get(0).getCount();
            log.debug("Found {} PRs for user: {}", count, githubLogin);
            return count;

        } catch (Exception e) {
            log.error("Error counting PRs for user: {}", githubLogin, e);
            return 0;
        }
    }

    /**
     * 사용자가 생성한 Issue 수 계산
     */
    public int countUserIssues(String githubLogin) {
        try {
            // MongoDB Aggregation Pipeline:
            // 1. $unwind: issues 배열을 개별 문서로 분리
            // 2. $match: user.login이 githubLogin과 일치하는 것만 필터링
            // 3. $count: 개수 세기
            
            Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.unwind("issues"),
                Aggregation.match(Criteria.where("issues.user.login").is(githubLogin)),
                Aggregation.count().as("count")
            );

            AggregationResults<CountResult> results = mongoTemplate.aggregate(
                aggregation,
                "github_issues_raw",
                CountResult.class
            );

            List<CountResult> resultList = results.getMappedResults();
            
            if (resultList.isEmpty()) {
                return 0;
            }

            int count = resultList.get(0).getCount();
            log.debug("Found {} issues for user: {}", count, githubLogin);
            return count;

        } catch (Exception e) {
            log.error("Error counting issues for user: {}", githubLogin, e);
            return 0;
        }
    }

    /**
     * 특정 연도의 사용자 PR 수 계산
     */
    public int countUserPRsByYear(String githubLogin, int year) {
        try {
            Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.unwind("pullRequests"),
                Aggregation.match(Criteria.where("pullRequests.user.login").is(githubLogin)
                    .and("pullRequests.created_at").regex("^" + year)),
                Aggregation.count().as("count")
            );

            AggregationResults<CountResult> results = mongoTemplate.aggregate(
                aggregation,
                "github_prs_raw",
                CountResult.class
            );

            List<CountResult> resultList = results.getMappedResults();
            
            if (resultList.isEmpty()) {
                return 0;
            }

            int count = resultList.get(0).getCount();
            log.debug("Found {} PRs for user: {} in year: {}", count, githubLogin, year);
            return count;

        } catch (Exception e) {
            log.error("Error counting PRs for user: {} in year: {}", githubLogin, year, e);
            return 0;
        }
    }

    /**
     * 특정 연도의 사용자 Issue 수 계산
     */
    public int countUserIssuesByYear(String githubLogin, int year) {
        try {
            Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.unwind("issues"),
                Aggregation.match(Criteria.where("issues.user.login").is(githubLogin)
                    .and("issues.created_at").regex("^" + year)),
                Aggregation.count().as("count")
            );

            AggregationResults<CountResult> results = mongoTemplate.aggregate(
                aggregation,
                "github_issues_raw",
                CountResult.class
            );

            List<CountResult> resultList = results.getMappedResults();
            
            if (resultList.isEmpty()) {
                return 0;
            }

            int count = resultList.get(0).getCount();
            log.debug("Found {} issues for user: {} in year: {}", count, githubLogin, year);
            return count;

        } catch (Exception e) {
            log.error("Error counting issues for user: {} in year: {}", githubLogin, year, e);
            return 0;
        }
    }

    /**
     * Aggregation 결과를 받기 위한 내부 클래스
     */
    private static class CountResult {
        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
