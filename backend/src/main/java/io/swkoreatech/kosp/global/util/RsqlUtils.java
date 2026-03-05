package io.swkoreatech.kosp.global.util;

import io.swkoreatech.kosp.common.exception.GlobalException;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import cz.jirutka.rsql.parser.RSQLParser;

import io.github.perplexhub.rsql.RSQLJPASupport;

/**
 * RSQL 필터 문자열을 JPA {@link Specification}으로 변환하는 유틸리티 클래스.
 * <p>RSQL 문법 검증 후 {@code RSQLJPASupport}를 사용하여 JPA Specification으로 변환한다.</p>
 */
public final class RsqlUtils {

    private static final RSQLParser PARSER = new RSQLParser();

    private RsqlUtils() {
    }

    /**
     * RSQL 필터 문자열을 JPA Specification으로 변환한다.
     *
     * @param <T>    엔티티 타입
     * @param filter RSQL 필터 문자열 (null 또는 빈 문자열이면 조건 없는 Specification 반환)
     * @return JPA Specification
     * @throws GlobalException RSQL 문법이 올바르지 않은 경우
     */
    public static <T> Specification<T> toSpecification(String filter) {
        if (filter == null || filter.isBlank()) {
            return (root, query, cb) -> null;
        }
        validateRsql(filter);
        return RSQLJPASupport.toSpecification(filter);
    }

    /**
     * RSQL 필터 문자열을 기존 Specification과 AND 조합하여 반환한다.
     *
     * @param <T>    엔티티 타입
     * @param filter RSQL 필터 문자열 (null 또는 빈 문자열이면 base Specification 반환)
     * @param base   기존 Specification
     * @return 조합된 JPA Specification
     * @throws GlobalException RSQL 문법이 올바르지 않은 경우
     */
    public static <T> Specification<T> toSpecification(String filter, Specification<T> base) {
        if (filter == null || filter.isBlank()) {
            return base;
        }
        validateRsql(filter);
        return base.and(RSQLJPASupport.toSpecification(filter));
    }

    private static void validateRsql(String filter) {
        try {
            PARSER.parse(filter);
        } catch (Exception e) {
            throw new GlobalException("잘못된 RSQL 문법입니다: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
