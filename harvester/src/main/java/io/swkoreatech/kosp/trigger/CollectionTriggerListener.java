package io.swkoreatech.kosp.trigger;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionTriggerListener {

    private static final String CHANNEL = "github_collection_trigger";

    // private final ConnectionFactory connectionFactory;
    // private final PriorityJobLauncher jobLauncher;
    //
    // private Disposable subscription;
    //
    // @EventListener(ApplicationReadyEvent.class)
    // public void startListening() {
    //     this.subscription = listenLoop()
    //         .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5)) // [핵심 변경 3] 무한 재연결 로직 추가
    //             .doBeforeRetry(signal -> log.warn("Lost connection to PostgreSQL LISTEN, retrying... ({})", signal.totalRetries())))
    //         .subscribe(
    //             null, // onNext는 listenLoop 내부에서 처리하므로 여기선 무시
    //             e -> log.error("Critical error in LISTEN loop", e)
    //         );
    //
    //     log.info("Started listening on PostgreSQL channel: {}", CHANNEL);
    // }
    //
    // private Mono<Void> listenLoop() {
    //     return Mono.from(connectionFactory.create())
    //         .flatMap(connection -> {
    //             // ConnectionPool을 사용하더라도 PostgresqlConnection으로 캐스팅 가능
    //             PostgresqlConnection pgConnection = (PostgresqlConnection) connection;
    //
    //             log.info("Connected to PostgreSQL for LISTEN on channel: {}", CHANNEL);
    //
    //             // LISTEN 명령 실행
    //             return pgConnection.createStatement("LISTEN " + CHANNEL)
    //                 .execute()
    //                 .flatMap(io.r2dbc.spi.Result::getRowsUpdated) // 결과 소비 필수
    //                 .thenMany(pgConnection.getNotifications()) // 알림 스트림 구독
    //                 .doOnNext(this::handleNotification)
    //                 .doOnCancel(() -> closeConnection(pgConnection)) // 구독 취소 시 연결 닫기
    //                 .doOnError(e -> log.error("Error receiving notification", e))
    //                 .then(); // 스트림이 끝나면 Mono<Void> 반환 (보통 연결 끊김 시)
    //         });
    // }
    //
    // private void handleNotification(Notification notification) {
    //     try {
    //         String payload = notification.getParameter();
    //         if (payload == null) return;
    //
    //         Long userId = Long.parseLong(payload);
    //         log.info("Received trigger for user: {}", userId);
    //         jobLauncher.submit(userId, Priority.HIGH);
    //     } catch (Exception e) {
    //         log.error("Failed to process notification: {}", notification.getParameter(), e);
    //     }
    // }
    //
    // private void closeConnection(PostgresqlConnection connection) {
    //     if (connection != null) {
    //         connection.close().subscribe();
    //     }
    // }
    //
    // @PreDestroy
    // public void stopListening() {
    //     if (subscription != null && !subscription.isDisposed()) {
    //         subscription.dispose();
    //     }
    //     log.info("Stopped listening on PostgreSQL channel: {}", CHANNEL);
    // }
}
