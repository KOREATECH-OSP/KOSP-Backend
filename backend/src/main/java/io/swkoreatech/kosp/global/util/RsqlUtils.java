package io.swkoreatech.kosp.global.util;

import org.springframework.data.jpa.domain.Specification;

import io.github.perplexhub.rsql.RSQLJPASupport;

public final class RsqlUtils {

    private RsqlUtils() {
    }

    public static <T> Specification<T> toSpecification(String filter) {
        if (filter == null || filter.isBlank()) {
            return (root, query, cb) -> null;
        }
        return RSQLJPASupport.toSpecification(filter);
    }

    public static <T> Specification<T> toSpecification(String filter, Specification<T> base) {
        if (filter == null || filter.isBlank()) {
            return base;
        }
        return base.and(RSQLJPASupport.toSpecification(filter));
    }
}
