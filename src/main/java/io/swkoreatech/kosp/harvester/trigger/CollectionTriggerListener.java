package io.swkoreatech.kosp.harvester.trigger;

import io.swkoreatech.kosp.harvester.launcher.Priority;
import io.swkoreatech.kosp.harvester.launcher.PriorityJobLauncher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionTriggerListener implements StreamListener<String, MapRecord<String, String, String>> {
    
    private final PriorityJobLauncher jobLauncher;
    
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            String userIdStr = message.getValue().get("userId");
            if (userIdStr == null) {
                log.warn("Received message without userId: {}", message.getId());
                return;
            }
            
            Long userId = Long.parseLong(userIdStr);
            log.info("Received collection trigger for user {} from stream", userId);
            
            jobLauncher.submit(userId, Priority.HIGH);
        } catch (NumberFormatException e) {
            log.error("Invalid userId format in message: {}", message.getId(), e);
        } catch (Exception e) {
            log.error("Failed to process collection trigger: {}", message.getId(), e);
        }
    }
}
