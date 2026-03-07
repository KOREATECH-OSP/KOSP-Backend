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

import io.swkoreatech.kosp.common.auth.model.Permission;
import io.swkoreatech.kosp.common.auth.model.Policy;
import io.swkoreatech.kosp.common.auth.model.Role;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security UserDetailsService 구현체.
 * <p>이메일(kutEmail)을 기반으로 사용자를 조회하고 권한을 설정한다.
 * SUPERUSER 역할을 가진 사용자에게는 와일드카드(*) 권한이 부여된다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /** {@inheritDoc} */
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
