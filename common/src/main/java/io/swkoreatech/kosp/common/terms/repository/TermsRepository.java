package io.swkoreatech.kosp.common.terms.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.terms.model.Terms;

/**
 * {@link Terms} 엔티티 데이터 접근 레포지토리.
 */
public interface TermsRepository extends Repository<Terms, Long> {

    Optional<Terms> findByIsActiveTrue();

    Optional<Terms> findByVersion(String version);

    default Terms getActive() {
        return findByIsActiveTrue()
            .orElseThrow(() -> new GlobalException(ExceptionMessage.TERMS_NOT_FOUND));
    }

    default Terms getByVersion(String version) {
        return findByVersion(version)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.TERMS_VERSION_NOT_FOUND));
    }
}
