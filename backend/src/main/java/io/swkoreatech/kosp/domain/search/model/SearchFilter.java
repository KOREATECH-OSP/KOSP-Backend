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

    /**
     * JSON 직렬화 시 사용되는 필터 값을 반환한다.
     *
     * @return 필터 이름 문자열
     */
    @JsonValue
    public String getValue() {
        return name();
    }

    /**
     * 문자열 값으로부터 {@link SearchFilter}를 생성한다.
     *
     * @param value 필터 이름 문자열
     * @return 대응하는 {@link SearchFilter} 열거값
     */
    @JsonCreator
    public static SearchFilter fromValue(String value) {
        return valueOf(value.toLowerCase());
    }
}
