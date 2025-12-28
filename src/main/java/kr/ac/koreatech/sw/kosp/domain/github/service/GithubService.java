package kr.ac.koreatech.sw.kosp.domain.github.service;



import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubActivity;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubActivityRepository;
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

    private final GithubActivityRepository githubActivityRepository;
    private final UserRepository userRepository;

    /**
     * GitHub API를 통해 활동 데이터를 수집하고 저장합니다.
     * (현재는 Mock 데이터로 시뮬레이션 구현, 실제 API 연동 시 교체 필요)
     */
    /**
     * GitHub API를 통해 활동 데이터를 수집하고 저장합니다.
     */
    @Transactional
    public void updateActivity(Long userId) {
        User user = findUserOrThrow(userId);
        
        if (hasNoGithubAccount(user)) {
             log.warn("User {} has no linked GitHub account.", userId);
             return;
        }

        processActivityUpdate(user);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
    }

    private boolean hasNoGithubAccount(User user) {
        return user.getGithubUser() == null;
    }

    private void processActivityUpdate(User user) {
        // Mock data for Phase 2
        long mockCommits = (long) (Math.random() * 500);
        long mockStars = (long) (Math.random() * 50);

        GithubActivity activity = getOrCreateActivity(user);
        activity.update(mockCommits, 20L, 5L, mockStars, 3L);
        
        githubActivityRepository.save(activity);
        log.info("Updated GitHub activity for user {}: commits={}", user.getId(), mockCommits);
    }

    private GithubActivity getOrCreateActivity(User user) {
        return githubActivityRepository.findByUser(user)
            .orElse(GithubActivity.builder().user(user).build());
    }
}
