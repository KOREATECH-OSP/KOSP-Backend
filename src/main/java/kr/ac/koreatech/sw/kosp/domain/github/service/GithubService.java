package kr.ac.koreatech.sw.kosp.domain.github.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GithubService {

    private final kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository userRepository;
    private final kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubProfileRepository githubProfileRepository;

    /**
     * GitHub API를 통해 활동 데이터를 수집하고 저장합니다.
     * (현재는 Spring Batch Job을 통해 일괄 처리됩니다.)
     */
    public void updateActivity(Long userId) {
        // Deprecated: GitHub Activity Sync is now handled by Spring Batch (GithubSyncJob).
        log.info("Manual update requested for user {}, but batch job handles syncing.", userId);
    }

    @Transactional(readOnly = true)
    public kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubAnalysisResponse getAnalysis(Long userId) {
        kr.ac.koreatech.sw.kosp.domain.user.model.User user = userRepository.getById(userId);

        if (user.getGithubUser() == null) {
            throw new kr.ac.koreatech.sw.kosp.global.exception.GlobalException(
                kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.GITHUB_USER_NOT_FOUND);
        }

        Long githubId = user.getGithubUser().getGithubId();
        kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubProfile profile = githubProfileRepository.findByGithubId(githubId)
            .orElseThrow(() -> new kr.ac.koreatech.sw.kosp.global.exception.GlobalException(
                kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.NOT_FOUND));

        return kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubAnalysisResponse.from(profile);
    }
}
