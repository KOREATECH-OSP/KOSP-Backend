package io.swkoreatech.kosp.domain.search.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SearchFilter {
    articles,
    recruits,
    teams,
    challenges,
    repositories;

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static SearchFilter fromValue(String value) {
        return valueOf(value.toLowerCase());
    }
}
