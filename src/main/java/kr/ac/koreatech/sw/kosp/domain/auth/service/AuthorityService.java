package kr.ac.koreatech.sw.kosp.domain.auth.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import kr.ac.koreatech.sw.kosp.domain.auth.model.Permission;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthorityService {

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
