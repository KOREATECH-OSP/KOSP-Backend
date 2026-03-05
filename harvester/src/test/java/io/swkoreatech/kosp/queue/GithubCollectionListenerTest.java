package io.swkoreatech.kosp.queue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;

import com.rabbitmq.client.Channel;

import io.swkoreatech.kosp.common.event.GithubCollectionRequest;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.launcher.PriorityJobLauncher;

@DisplayName("GithubCollectionListener 단위 테스트")
@ExtendWith(MockitoExtension.class)
class GithubCollectionListenerTest {

    @Mock
    private PriorityJobLauncher jobLauncher;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobExplorer jobExplorer;

    @InjectMocks
    private GithubCollectionListener listener;

    @Nested
    @DisplayName("handleCollectionRequest 메서드")
    class HandleCollectionRequestTest {

        @Test
        @DisplayName("정상 케이스: 사용자가 존재하고 삭제되지 않았으며 실행 중인 잡이 없으면 잡을 실행한다")
        void launchesJob_whenUserExistsAndNotDeletedAndNoRunningJob() throws Exception {
            Long userId = 1L;
            Channel channel = mock(Channel.class);
            GithubCollectionRequest request = new GithubCollectionRequest(userId);

            User user = createActiveUser(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(jobExplorer.findRunningJobExecutions("githubCollectionJob")).thenReturn(Set.of());

            listener.handleCollectionRequest(request, 1L, channel);

            verify(jobLauncher).run(eq(userId), any(String.class));
            verify(channel).basicAck(1L, false);
        }

        @Test
        @DisplayName("삭제된 사용자: isDeleted가 true이면 잡을 실행하지 않고 ack한다")
        void acksWithoutLaunch_whenUserIsDeleted() throws Exception {
            Long userId = 2L;
            Channel channel = mock(Channel.class);
            GithubCollectionRequest request = new GithubCollectionRequest(userId);

            User user = createDeletedUser(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            listener.handleCollectionRequest(request, 2L, channel);

            verify(jobLauncher, never()).run(any(), any());
            verify(channel).basicAck(2L, false);
        }

        @Test
        @DisplayName("사용자 없음: findById가 empty를 반환하면 잡을 실행하지 않고 ack한다")
        void acksWithoutLaunch_whenUserNotFound() throws Exception {
            Long userId = 3L;
            Channel channel = mock(Channel.class);
            GithubCollectionRequest request = new GithubCollectionRequest(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            listener.handleCollectionRequest(request, 3L, channel);

            verify(jobLauncher, never()).run(any(), any());
            verify(channel).basicAck(3L, false);
        }

        @Test
        @DisplayName("잡 이미 실행 중: 동일 userId로 실행 중인 잡이 있으면 잡을 실행하지 않고 ack한다")
        void acksWithoutLaunch_whenJobAlreadyRunningForUser() throws Exception {
            Long userId = 4L;
            Channel channel = mock(Channel.class);
            GithubCollectionRequest request = new GithubCollectionRequest(userId);

            User user = createActiveUser(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            JobExecution runningExecution = mock(JobExecution.class);
            JobParameters jobParameters = mock(JobParameters.class);
            when(runningExecution.getJobParameters()).thenReturn(jobParameters);
            when(jobParameters.getLong("userId")).thenReturn(userId);
            when(jobExplorer.findRunningJobExecutions("githubCollectionJob"))
                .thenReturn(Set.of(runningExecution));

            listener.handleCollectionRequest(request, 4L, channel);

            verify(jobLauncher, never()).run(any(), any());
            verify(channel).basicAck(4L, false);
        }

        @Test
        @DisplayName("예외 발생: jobLauncher.run()이 예외를 던지면 basicNack를 호출한다")
        void callsBasicNack_whenJobLauncherThrowsException() throws Exception {
            Long userId = 5L;
            Channel channel = mock(Channel.class);
            GithubCollectionRequest request = new GithubCollectionRequest(userId);

            User user = createActiveUser(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(jobExplorer.findRunningJobExecutions("githubCollectionJob")).thenReturn(Set.of());
            org.mockito.Mockito.doThrow(new RuntimeException("launch failed"))
                .when(jobLauncher).run(any(), any());

            listener.handleCollectionRequest(request, 5L, channel);

            verify(channel).basicNack(5L, false, false);
            verify(channel, never()).basicAck(any(Long.class), any(Boolean.class));
        }
    }

    private User createActiveUser(Long userId) {
        return User.builder()
            .id(userId)
            .name("User " + userId)
            .kutId(String.valueOf(userId))
            .kutEmail("user" + userId + "@koreatech.ac.kr")
            .password("tempPassword123!")
            .build();
    }

    private User createDeletedUser(Long userId) {
        User user = User.builder()
            .id(userId)
            .name("User " + userId)
            .kutId(String.valueOf(userId))
            .kutEmail("user" + userId + "@koreatech.ac.kr")
            .password("tempPassword123!")
            .build();
        user.delete();
        return user;
    }
}
