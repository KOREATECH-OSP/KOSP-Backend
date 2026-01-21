package kr.ac.koreatech.sw.kosp.domain.github.collection.trigger;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import kr.ac.koreatech.sw.kosp.domain.github.collection.launcher.JobPriority;
import kr.ac.koreatech.sw.kosp.domain.github.collection.launcher.PriorityJobLauncher;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.domain.user.event.UserSignupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubCollectionEventListener {

    private final PriorityJobLauncher priorityJobLauncher;
    private final GithubUserRepository githubUserRepository;

    @EventListener
    public void handleUserSignup(UserSignupEvent event) {
        String githubLogin = event.getGithubLogin();
        log.info("Received UserSignupEvent for {}", githubLogin);

        githubUserRepository.findByGithubLogin(githubLogin)
            .ifPresentOrElse(
                githubUser -> submitHighPriorityJob(githubUser),
                () -> log.warn("GithubUser not found for login: {}", githubLogin)
            );
    }

    private void submitHighPriorityJob(GithubUser githubUser) {
        priorityJobLauncher.submit(githubUser.getGithubId(), JobPriority.HIGH);
        log.info("Submitted HIGH priority job for user {}", githubUser.getGithubLogin());
    }
}
