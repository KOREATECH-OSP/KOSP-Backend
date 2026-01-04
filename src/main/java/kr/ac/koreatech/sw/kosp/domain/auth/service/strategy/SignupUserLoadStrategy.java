package kr.ac.koreatech.sw.kosp.domain.auth.service.strategy;

import static kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.AUTHENTICATION;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Component("signup")
@RequiredArgsConstructor
public class SignupUserLoadStrategy implements UserLoadStrategy {

    private final GithubUserRepository githubUserRepository;

    @Override
    public UserDetails loadUser(String id) {
        GithubUser githubUser = githubUserRepository.findByGithubId(Long.parseLong(id))
            .orElseThrow(() -> new GlobalException(AUTHENTICATION.getMessage(), AUTHENTICATION.getStatus()));

        // Return a temporary UserDetails with limited role for Signup completion
        return User.builder()
            .username(githubUser.getGithubLogin()) // or another identifier
            .password("") // ID-only authentication
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_PENDING")))
            .build();
    }
}
