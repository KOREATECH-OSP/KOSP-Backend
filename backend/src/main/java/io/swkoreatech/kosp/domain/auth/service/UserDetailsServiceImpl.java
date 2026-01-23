package io.swkoreatech.kosp.domain.auth.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.auth.model.Permission;
import io.swkoreatech.kosp.domain.auth.model.Policy;
import io.swkoreatech.kosp.domain.auth.model.Role;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByKutEmail(username)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.AUTHENTICATION));

        // 탈퇴한 계정 체크
        if (user.isDeleted()) {
            throw new GlobalException(ExceptionMessage.AUTHENTICATION);
        }

        user.setAuthorities(getAuthorities(user.getRoles()));
        return user;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        Set<String> authorities = new HashSet<>();

        // SUPERUSER 체크: ROLE_SUPERUSER를 가진 사용자는 모든 권한 보유
        boolean isSuperuser = roles.stream()
            .anyMatch(role -> "ROLE_SUPERUSER".equals(role.getName()));

        if (isSuperuser) {
            authorities.add("*");
            log.debug("SUPERUSER detected, granting wildcard authority");
            return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        }

        for (Role role : roles) {
            processRole(role, authorities);
        }

        return authorities.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());
    }

    private void processRole(Role role, Set<String> authorities) {
        authorities.add(role.getName());
        for (Policy policy : role.getPolicies()) {
            extractPermissions(policy, authorities);
        }
    }

    private void extractPermissions(Policy policy, Set<String> authorities) {
        Set<String> perms = policy.getPermissions().stream()
            .map(Permission::getName)
            .collect(Collectors.toSet());
        authorities.addAll(perms);
    }
}
