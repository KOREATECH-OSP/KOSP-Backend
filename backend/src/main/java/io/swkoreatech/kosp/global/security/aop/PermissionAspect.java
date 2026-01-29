package io.swkoreatech.kosp.global.security.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.domain.auth.service.PermissionService;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;

    @Before("@annotation(permit)")
    public void checkPermission(JoinPoint joinPoint, Permit permit) {
        log.info("🔒 [PERMIT] Checking permission for: {} | permitAll: {} | permission: {}", 
            joinPoint.getSignature().toShortString(), permit.permitAll(), permit.name());
        
        if (permit.permitAll()) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            log.info("🔒 [PERMIT] Authentication is NULL in SecurityContext");
        } else if (!authentication.isAuthenticated()) {
            log.info("🔒 [PERMIT] Authentication exists but not authenticated");
        } else if ("anonymousUser".equals(authentication.getPrincipal())) {
            log.info("🔒 [PERMIT] Authentication is anonymousUser");
        } else {
            log.info("🔒 [PERMIT] Authenticated: {} ({})", 
                authentication.getPrincipal().getClass().getSimpleName(),
                authentication.isAuthenticated());
        }

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new GlobalException(ExceptionMessage.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User user)) {
            // Should not happen if authenticated properly with our User model
            throw new GlobalException(ExceptionMessage.AUTHENTICATION);
        }

        // SUPERUSER 체크: ROLE_SUPERUSER는 모든 권한 보유
        boolean isSuperuser = user.getRoles().stream()
            .anyMatch(role -> "ROLE_SUPERUSER".equals(role.getName()));
        
        if (isSuperuser) {
            log.debug("SUPERUSER access granted for: {}", permit.name());
            return;
        }

        if (permit.name().isEmpty()) {
            // No specific permission required, just authentication
            return;
        }

        boolean hasPermission = permissionService.hasPermission(user.getId(), permit.name());
        if (!hasPermission) {
            log.warn("Access Denied: User {} (ID: {}) tried to access {} without permission {}", 
                user.getName(), user.getId(), joinPoint.getSignature(), permit.name());
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }
}
