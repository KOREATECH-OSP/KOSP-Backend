package kr.ac.koreatech.sw.kosp.domain.github.batch;

import kr.ac.koreatech.sw.kosp.domain.github.dto.UserSyncResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSyncWriter implements ItemWriter<UserSyncResult> {

    private final MongoTemplate mongoTemplate;

    @Override
    public void write(Chunk<? extends UserSyncResult> chunk) throws Exception {
        for (UserSyncResult item : chunk) {
            if (item == null) continue;

            // 1. Save Profile
            if (item.profile() != null) {
                mongoTemplate.save(item.profile());
            }

            // 2. Save Repositories
            if (item.repositories() != null && !item.repositories().isEmpty()) {
                // Using save (upsert) for each repository
                // Optimization: In a real heavy-load system, bulkOps should be used.
                for (var repo : item.repositories()) {
                    mongoTemplate.save(repo);
                }
            }
            
            log.debug("Synced user {} with {} repositories", item.profile().getGithubId(), item.repositories().size());
        }
    }
}
