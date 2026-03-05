package io.swkoreatech.kosp.domain.auth.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.auth.model.Permission;
import io.swkoreatech.kosp.common.auth.model.Policy;
import io.swkoreatech.kosp.common.auth.model.Role;
import lombok.RequiredArgsConstructor;

/**
 * 권한 변환 서비스.
 * <p>사용자의 역할(Role) 집합으로부터 Spring Security의 {@link GrantedAuthority} 컬렉션을 생성한다.</p>
 */
@Component
@RequiredArgsConstructor
public class AuthorityService {

    /**
     * 역할 집합으로부터 권한 목록을 추출한다.
     *
     * @param roles 사용자 역할 집합
     * @return Spring Security 권한 컬렉션
     */
    public Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        Set<String> authorities = new HashSet<>();
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
