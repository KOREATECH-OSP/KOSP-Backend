package io.swkoreatech.kosp.trigger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.launcher.Priority;
import io.swkoreatech.kosp.launcher.PriorityJobLauncher;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionTriggerListener {

    private static final String CHANNEL = "github_collection_trigger";
    private static final int POLL_INTERVAL_MS = 500;

    private final DataSource dataSource;
    private final PriorityJobLauncher jobLauncher;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;
    private Connection connection;

    @EventListener(ApplicationReadyEvent.class)
    public void startListening() {
        executor.submit(this::listenLoop);
        log.info("Started PostgreSQL LISTEN on channel: {}", CHANNEL);
    }

    private void listenLoop() {
        while (running) {
            try {
                ensureConnection();
                pollNotifications();
            } catch (Exception e) {
                log.error("Error in LISTEN loop, reconnecting in 5s...", e);
                closeConnection();
                sleep(5000);
            }
        }
    }

    private void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = dataSource.getConnection();
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("LISTEN " + CHANNEL);
            }
            log.info("Connected and listening on channel: {}", CHANNEL);
        }
    }

    private void pollNotifications() throws SQLException {
        PGConnection pgConn = connection.unwrap(PGConnection.class);
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("");
        }

        PGNotification[] notifications = pgConn.getNotifications();
        if (notifications == null) {
            sleep(POLL_INTERVAL_MS);
            return;
        }

        for (PGNotification notification : notifications) {
            handleNotification(notification);
        }
    }

    private void handleNotification(PGNotification notification) {
        try {
            String payload = notification.getParameter();
            if (payload == null || payload.isEmpty()) {
                return;
            }

            Long userId = Long.parseLong(payload);
            log.info("Received trigger for user: {}", userId);
            jobLauncher.submit(userId, Priority.HIGH);
        } catch (NumberFormatException e) {
            log.error("Invalid userId in notification: {}", notification.getParameter(), e);
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.debug("Error closing connection", e);
            }
            connection = null;
        }
    }

    @PreDestroy
    public void stopListening() {
        running = false;
        closeConnection();
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Stopped listening on channel: {}", CHANNEL);
    }
}
