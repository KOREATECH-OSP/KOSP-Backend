package io.swkoreatech.kosp.global.security.aop;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.auth.service.PermissionService;
import io.swkoreatech.kosp.global.security.annotation.Permit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
        if (permit.permitAll()) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new GlobalException(ExceptionMessage.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User user)) {
            throw new GlobalException(ExceptionMessage.AUTHENTICATION);
        }

        boolean isSuperuser = user.getRoles().stream()
            .anyMatch(role -> "ROLE_SUPERUSER".equals(role.getName()));
        
        if (isSuperuser) {
            log.debug("SUPERUSER access granted for: {}", permit.name());
            return;
        }

        if (permit.name().isEmpty()) {
            return;
        }

        boolean hasPermission = permissionService.hasPermission(user.getId(), permit.name());
        if (!hasPermission) {
            log.warn(
                    "Access Denied: User {} (ID: {}) tried to access {} without permission {}",
                    user.getName(), user.getId(), joinPoint.getSignature(), permit.name()
            );
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }
}
