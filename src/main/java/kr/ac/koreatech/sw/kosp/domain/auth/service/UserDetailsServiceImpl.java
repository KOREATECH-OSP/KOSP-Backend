package kr.ac.koreatech.sw.kosp.domain.auth.service;

import static kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.AUTHENTICATION;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.user.model.User;
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
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Integer id = Integer.parseInt(userId);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new GlobalException(AUTHENTICATION.getMessage(), AUTHENTICATION.getStatus()));

        Collection<GrantedAuthority> authorities = Collections.emptyList();

        return new org.springframework.security.core.userdetails.User(
            user.getName(),
            user.getPassword(),
            !user.isDeleted(),
            true,
            true,
            true,
            authorities
        );
    }

}
