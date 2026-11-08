package io.swkoreatech.kosp.domain.material.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.github.dto.response.GithubResumeProjectResponse;
import io.swkoreatech.kosp.domain.github.service.GithubStatisticsService;
import io.swkoreatech.kosp.domain.material.model.MaterialItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 학습자료 ↔ GitHub 프로젝트 중복 가능성 판정기.
 *
 * <p>정책상 자동 병합은 하지 않는다. 학교 자료(과제/EL)의 제목이 사용자의 GitHub
 * 저장소명과 충분히 유사하면 자료 쪽에 "중복 가능성 있음" 안내 플래그만 설정하고,
 * 최종 판단/수정은 사용자에게 맡긴다.</p>
 *
 * <p>판정 기준: 제목 토큰 자카드(Jaccard) 유사도 &ge; {@value #DUPLICATE_THRESHOLD}.
 * 학교 과제는 repo URL/기술스택이 비어 있는 경우가 많아, 안정적인 신호인 제목 유사도를 사용한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaterialDuplicateDetector {

    /** 중복 안내를 띄우는 제목 유사도 임계값. */
    private static final double DUPLICATE_THRESHOLD = 0.6;

    private final GithubStatisticsService githubStatisticsService;

    /**
     * 자료 목록 각각에 대해 GitHub 저장소와의 중복 가능성을 판정하고 플래그를 갱신한다.
     * 재판정이므로 더 이상 유사하지 않으면 플래그를 해제한다.
     */
    public void detect(User user, List<MaterialItem> items) {
        if (items.isEmpty()) {
            return;
        }
        List<GithubResumeProjectResponse> repos = githubStatisticsService.getMyRepositoriesForResume(user);
        if (repos.isEmpty()) {
            items.forEach(item -> item.markDuplicate(false, null));
            return;
        }

        for (MaterialItem item : items) {
            Set<String> titleTokens = tokenize(item.getTitle());
            String bestRepoKey = null;
            double bestScore = 0.0;
            for (GithubResumeProjectResponse repo : repos) {
                double score = jaccard(titleTokens, tokenize(repo.name()));
                if (score > bestScore) {
                    bestScore = score;
                    bestRepoKey = repo.repoKey();
                }
            }
            boolean duplicated = bestScore >= DUPLICATE_THRESHOLD;
            item.markDuplicate(duplicated, bestRepoKey);
            if (duplicated) {
                log.info("자료-GitHub 중복 가능성 감지: itemId={}, repoKey={}, score={}",
                    item.getId(), bestRepoKey, bestScore);
            }
        }
    }

    /**
     * 제목을 정규화 토큰 집합으로 분해한다 (소문자화, 영문/숫자/한글 외 구분자 분리).
     */
    private Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        String[] parts = text.toLowerCase().split("[^0-9a-z가-힣]+");
        Set<String> tokens = new HashSet<>();
        Arrays.stream(parts)
            .filter(token -> !token.isBlank())
            .forEach(tokens::add);
        return tokens;
    }

    /**
     * 두 토큰 집합의 자카드 유사도 (|교집합| / |합집합|).
     */
    private double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        int union = a.size() + b.size() - intersection.size();
        return union == 0 ? 0.0 : (double) intersection.size() / union;
    }
}
