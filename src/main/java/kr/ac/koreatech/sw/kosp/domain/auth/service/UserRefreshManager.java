package kr.ac.koreatech.sw.kosp.domain.auth.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class UserRefreshManager {

    private final Set<String> dirtyUsernames = ConcurrentHashMap.newKeySet();

    public void markAsDirty(String username) {
        dirtyUsernames.add(username);
    }

    public boolean isDirty(String username) {
        return dirtyUsernames.contains(username);
    }

    public void clear(String username) {
        dirtyUsernames.remove(username);
    }
}
