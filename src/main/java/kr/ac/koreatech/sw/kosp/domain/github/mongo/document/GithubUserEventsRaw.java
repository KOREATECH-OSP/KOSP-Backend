package kr.ac.koreatech.sw.kosp.domain.github.mongo.document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "github_user_events_raw")
@Data
@NoArgsConstructor
public class GithubUserEventsRaw {
    
    @Id
    private String id;
    
    private String githubLogin;
    private List<Map<String, Object>> events;
    private LocalDateTime collectedAt;
    
    public static GithubUserEventsRaw create(
        String githubLogin,
        List<Map<String, Object>> events
    ) {
        GithubUserEventsRaw raw = new GithubUserEventsRaw();
        raw.githubLogin = githubLogin;
        raw.events = events;
        raw.collectedAt = LocalDateTime.now();
        return raw;
    }
}
