package kr.ac.koreatech.sw.kosp.domain.search.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SearchSortType {
    relevance,
    date_desc,
    date_asc;

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static SearchSortType fromValue(String value) {
        return valueOf(value.toLowerCase());
    }
}
