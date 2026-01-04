package kr.ac.koreatech.sw.kosp.global.security.aop;

import kr.ac.koreatech.sw.kosp.domain.auth.service.PermissionService;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new GlobalException(ExceptionMessage.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User user)) {
            // Should not happen if authenticated properly with our User model
            throw new GlobalException(ExceptionMessage.AUTHENTICATION);
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
