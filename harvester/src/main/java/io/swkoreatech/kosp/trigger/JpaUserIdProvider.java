package io.swkoreatech.kosp.trigger;

import io.swkoreatech.kosp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JpaUserIdProvider implements UserIdProvider {

    private final UserRepository userRepository;

    @Override
    public List<Long> findActiveUserIds() {
        return userRepository.findActiveUserIds();
    }
}
