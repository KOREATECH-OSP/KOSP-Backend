package kr.ac.koreatech.sw.kosp.global.security.aspect;

import java.util.Collection;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class PermissionCheckAspect {

    @Before("@annotation(permit)")
    public void checkPermission(JoinPoint joinPoint, Permit permit) {
        if (permit.permitAll()) {
            return; // Public access
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String requiredPermission = permit.name();
        if (!requiredPermission.isEmpty()) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            
            // SUPERUSER 체크: "*" 권한이 있으면 모든 권한 보유
            boolean isSuperuser = authorities.stream()
                .anyMatch(authority -> "*".equals(authority.getAuthority()));
            
            if (isSuperuser) {
                log.debug("SUPERUSER access granted for: {}", requiredPermission);
                return;
            }
            
            // 일반 권한 체크
            boolean hasPermission = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(requiredPermission));

            if (!hasPermission) {
                log.warn("Access denied. User: {}, Required: {}, Authorities: {}", 
                    authentication.getName(), requiredPermission, authorities);
                throw new AccessDeniedException("Access is denied: " + requiredPermission);
            }
        }
    }
}
