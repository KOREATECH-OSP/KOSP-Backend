package io.swkoreatech.kosp.queue;

import io.swkoreatech.kosp.common.event.GithubCollectionRequest;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import io.swkoreatech.kosp.launcher.PriorityJobLauncher;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ에서 GitHub 수집 요청 메시지를 수신하는 리스너.
 *
 * <p>삭제된 사용자나 이미 실행 중인 작업을 건너뛰고,
 * 유효한 요청에 대해 {@link PriorityJobLauncher}를 통해 배치 작업을 실행한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GithubCollectionListener {

    private final PriorityJobLauncher jobLauncher;
    private final UserRepository userRepository;
    private final JobExplorer jobExplorer;

    /**
     * GitHub 수집 요청 메시지를 처리한다.
     *
     * <p>삭제된 사용자 및 이미 실행 중인 작업을 확인한 후
     * 유효한 요청에 대해 배치 작업을 실행한다.
     *
     * @param request     수집 요청 DTO
     * @param deliveryTag 메시지 전달 태그
     * @param channel     RabbitMQ 채널
     * @throws IOException 채널 ACK/NACK 시 발생 가능
     */
    @RabbitListener(queues = QueueNames.GITHUB_COLLECTION)
    public void handleCollectionRequest(
            GithubCollectionRequest request,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
            Channel channel) throws IOException {
        try {
            if (isUserDeleted(request.userId())) {
                log.info("Skipping job for deleted user: {}", request.userId());
                channel.basicAck(deliveryTag, false);
                return;
            }
            if (isJobRunningForUser(request.userId())) {
                log.info("Job already running for user {}, discarding message", request.userId());
                channel.basicAck(deliveryTag, false);
                return;
            }
            String runId = UUID.randomUUID().toString();
            jobLauncher.run(request.userId(), runId);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to handle collection request: userId={}", request.userId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private boolean isUserDeleted(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return true;
        }
        return user.isDeleted();
    }

    private boolean isJobRunningForUser(Long userId) {
        Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("githubCollectionJob");
        return runningExecutions.stream()
            .anyMatch(execution -> {
                Long jobUserId = execution.getJobParameters().getLong("userId");
                return userId.equals(jobUserId);
            });
    }
}
