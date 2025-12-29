package kr.ac.koreatech.sw.kosp.domain.github.controller;

import io.swagger.v3.oas.annotations.Operation;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile({"dev", "local"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/test")
public class TestGithubController {

    private final JobLauncher jobLauncher;
    private final Job githubSyncJob;

    @Operation(summary = "GitHub Sync 수동 실행 (테스트용)", description = "Spring Batch Job을 수동으로 트리거합니다.")
    @PostMapping("/sync/github")
    @Permit // Authenticated users only
    public ResponseEntity<Void> syncGithub(@AuthUser User user) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addLong("triggerUser", user.getId())
                .toJobParameters();
            
            jobLauncher.run(githubSyncJob, jobParameters);
        } catch (Exception e) {
            throw new kr.ac.koreatech.sw.kosp.global.exception.GlobalException(kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.SERVER_ERROR);
        }
        return ResponseEntity.ok().build();
    }
}
