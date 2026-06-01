package io.swkoreatech.kosp.global.security.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.terms.service.TermsService;
import lombok.RequiredArgsConstructor;

/**
 * {@link TermsRequired} 어노테이션 기반의 약관 동의 검증 AOP 어스펙트.
 *
 * <p>메서드 실행 전에 인증된 사용자가 현재 유효한 약관에 동의했는지 확인한다.
 * 미동의 상태이면 {@code 403 Forbidden}을 반환한다.</p>
 */
@Aspect
@Component
@RequiredArgsConstructor
public class TermsAspect {

    private final TermsService termsService;

    /**
     * {@link TermsRequired} 어노테이션이 붙은 메서드 실행 전에 약관 동의 여부를 검증한다.
     *
     * @throws GlobalException 인증되지 않았거나 약관에 동의하지 않은 경우
     */
    @Before("@annotation(io.swkoreatech.kosp.global.security.aop.TermsRequired)")
    public void checkTermsAgreement() {
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

        if (termsService.needsTermsAgreement(user)) {
            throw new GlobalException(ExceptionMessage.TERMS_AGREEMENT_REQUIRED);
        }
    }
}
