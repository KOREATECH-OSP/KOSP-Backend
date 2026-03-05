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

/**
 * {@link Permit} 어노테이션 기반의 권한 검증 AOP 어스펙트.
 * <p>메서드 실행 전에 인증된 사용자가 필요한 권한을 보유하고 있는지 검증한다.
 * SUPERUSER 역할을 가진 사용자는 모든 권한 검사를 통과한다.</p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;

    /**
     * {@link Permit} 어노테이션이 붙은 메서드 실행 전에 권한을 검증한다.
     *
     * @param joinPoint 조인 포인트
     * @param permit    권한 어노테이션
     * @throws GlobalException 인증되지 않았거나 권한이 없는 경우
     */
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
