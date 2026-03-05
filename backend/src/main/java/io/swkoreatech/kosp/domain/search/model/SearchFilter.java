package io.swkoreatech.kosp.domain.search.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 검색 필터.
 * articles: 게시글, recruits: 모집글, teams: 팀,
 * challenges: 챌린지, users: 사용자, repositories: 저장소.
 */
public enum SearchFilter {

    articles,
    recruits,
    teams,
    challenges,
    users,
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
