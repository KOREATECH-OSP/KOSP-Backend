package kr.ac.koreatech.sw.kosp.domain.github.service;



import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubProfile;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubProfileRepository;
import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GithubService {

    private final GithubProfileRepository githubProfileRepository;
    private final UserRepository userRepository;

    /**
     * GitHub API를 통해 활동 데이터를 수집하고 저장합니다.
     * (현재는 Mock 데이터로 시뮬레이션 구현, 추후 GithubApiClient로 분리 예정)
     */
    @Transactional
    public void updateActivity(Long userId) {
        User user = findUserOrThrow(userId);
        
        if (hasNoGithubAccount(user)) {
             log.warn("User {} has no linked GitHub account.", userId);
             return;
        }
        
        // MongoDB Update (Mock)
        updateGithubProfile(user.getGithubUser().getGithubId());
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
    }

    private boolean hasNoGithubAccount(User user) {
        return user.getGithubUser() == null;
    }

    private void updateGithubProfile(Long githubId) {
        // Mock Data Creation
        GithubProfile profile = GithubProfile.builder()
            .githubId(githubId)
            .bio("Open Source Enthusiast")
            .tier(2) // Silver
            .followers(100)
            .following(50)
            .achievements(List.of("Pull Shark", "YOLO"))
            .stats(GithubProfile.Stats.builder()
                .totalCommits(150L)
                .totalIssues(10L)
                .totalPrs(5L)
                .totalStars(20L)
                .totalRepos(15L)
                .build())
            .score(1250.5)
            .build();

        githubProfileRepository.save(profile);
        log.info("Updated GitHub profile for githubId {}: tier={}", githubId, profile.getTier());
    }
}
