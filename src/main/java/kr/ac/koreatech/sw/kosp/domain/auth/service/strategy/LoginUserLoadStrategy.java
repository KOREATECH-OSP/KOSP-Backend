package kr.ac.koreatech.sw.kosp.domain.auth.service.strategy;

import static kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.AUTHENTICATION;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Component("login")
@RequiredArgsConstructor
public class LoginUserLoadStrategy implements UserLoadStrategy {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUser(String id) {
        return userRepository.findById(Long.parseLong(id))
            .orElseThrow(() -> new GlobalException(AUTHENTICATION.getMessage(), AUTHENTICATION.getStatus()));
    }
}
