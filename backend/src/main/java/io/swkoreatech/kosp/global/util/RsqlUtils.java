package io.swkoreatech.kosp.global.util;

import io.swkoreatech.kosp.common.exception.GlobalException;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import cz.jirutka.rsql.parser.RSQLParser;

import io.github.perplexhub.rsql.RSQLJPASupport;

public final class RsqlUtils {

    private static final RSQLParser PARSER = new RSQLParser();

    private RsqlUtils() {
    }

    public static <T> Specification<T> toSpecification(String filter) {
        if (filter == null || filter.isBlank()) {
            return (root, query, cb) -> null;
        }
        validateRsql(filter);
        return RSQLJPASupport.toSpecification(filter);
    }

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
