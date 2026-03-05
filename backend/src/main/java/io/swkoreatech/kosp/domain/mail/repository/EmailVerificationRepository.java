package io.swkoreatech.kosp.domain.mail.repository;

import org.springframework.data.repository.CrudRepository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.domain.mail.model.EmailVerification;

/**
 * 이메일 인증 리포지토리.
 * Redis 기반 이메일 인증 정보의 저장 및 조회 기능을 제공한다.
 */
public interface EmailVerificationRepository extends CrudRepository<EmailVerification, String> {

    /**
     * ID로 이메일 인증 정보를 조회하고, 없으면 예외를 발생시킨다.
     *
     * @param id 이메일 주소
     * @return 이메일 인증 정보
     * @throws GlobalException 인증 정보를 찾을 수 없는 경우
     */
    default EmailVerification getById(String id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.EMAIL_NOT_FOUND));
    }
}
