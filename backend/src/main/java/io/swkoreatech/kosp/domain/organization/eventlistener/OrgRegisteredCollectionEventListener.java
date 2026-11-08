package io.swkoreatech.kosp.domain.organization.eventlistener;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.swkoreatech.kosp.common.event.GithubOrgCollectionRequest;
import io.swkoreatech.kosp.domain.organization.event.OrgRegisteredCollectionEvent;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 조직 등록 이벤트 리스너.
 * 조직 등록 트랜잭션 커밋 후 RabbitMQ를 통해 레포지토리 수집 요청을 발행한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrgRegisteredCollectionEventListener {

    private final RabbitTemplate rabbitTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrgRegistered(OrgRegisteredCollectionEvent event) {
        log.info("OrgRegisteredCollectionEvent for org {} (id={})", event.getOrgName(), event.getOrganizationId());

        try {
            GithubOrgCollectionRequest dto = new GithubOrgCollectionRequest(
                event.getOrganizationId(),
                event.getOrgName(),
                event.getGithubOrgId(),
                event.getAdminUserId()
            );
            rabbitTemplate.convertAndSend(
                QueueNames.GITHUB_ORG_COLLECTION_EXCHANGE,
                QueueNames.GITHUB_ORG_COLLECTION,
                dto,
                message -> {
                    message.getMessageProperties().setHeader("x-delay", 0);
                    return message;
                }
            );
            log.info("Published org repo collection request for org {}", event.getOrgName());
        } catch (Exception e) {
            log.error("Failed to publish org repo collection request for org {}", event.getOrgName(), e);
        }
    }
}
