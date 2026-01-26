package io.swkoreatech.kosp.trigger;

import java.util.List;

import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaUserIdProvider implements UserIdProvider {

    private final UserRepository userRepository;

    @Override
    public List<Long> findActiveUserIds() {
        return userRepository.findActiveUserIds();
    }
}
