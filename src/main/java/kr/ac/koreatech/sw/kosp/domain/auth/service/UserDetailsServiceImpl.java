package kr.ac.koreatech.sw.kosp.domain.auth.service;

import static kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.AUTHENTICATION;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByKutEmail(email)
            .orElseThrow(() -> new GlobalException(AUTHENTICATION.getMessage(), AUTHENTICATION.getStatus()));
    }

}
