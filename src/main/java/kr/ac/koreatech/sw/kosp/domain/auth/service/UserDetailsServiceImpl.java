package kr.ac.koreatech.sw.kosp.domain.auth.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.auth.model.Permission;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.service.strategy.UserLoadStrategy;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.auth.model.AuthTokenCategory;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final Map<String, UserLoadStrategy> userLoadStrategies;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByKutEmail(username)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.AUTHENTICATION));
        
        user.setAuthorities(getAuthorities(user.getRoles()));
        return user;
    }

    @Transactional(readOnly = true)
    public UserDetails loadUser(String id, AuthTokenCategory category) {
        UserLoadStrategy strategy = userLoadStrategies.get(category.getValue());
        if (strategy == null) {
            throw new GlobalException(ExceptionMessage.AUTHENTICATION);
        }
        return strategy.loadUser(id);
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        Set<String> authorities = new HashSet<>();
        
        // SUPERUSER 체크: ROLE_SUPERUSER를 가진 사용자는 모든 권한 보유
        boolean isSuperuser = roles.stream()
            .anyMatch(role -> "ROLE_SUPERUSER".equals(role.getName()));
        
        if (isSuperuser) {
            authorities.add("*");  // 와일드카드 권한으로 모든 API 접근 허용
            log.debug("SUPERUSER detected, granting wildcard authority");
        } else {
            // 일반 사용자는 기존 로직으로 권한 수집
            for (Role role : roles) {
                processRole(role, authorities);
            }
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
