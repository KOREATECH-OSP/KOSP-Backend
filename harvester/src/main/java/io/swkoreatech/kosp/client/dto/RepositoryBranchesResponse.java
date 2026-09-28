package io.swkoreatech.kosp.client.dto;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

/**
 * GitHub GraphQL API의 저장소 브랜치 목록 응답 DTO.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryBranchesResponse {

    private Repository repository;

    /**
     * 브랜치의 qualified name 목록을 반환한다 (예: "refs/heads/main").
     *
     * @return qualified name 목록, 데이터 없으면 빈 목록
     */
    public List<String> getBranchQualifiedNames() {
        if (repository == null || repository.getRefs() == null
            || repository.getRefs().getNodes() == null) {
            return Collections.emptyList();
        }
        return repository.getRefs().getNodes().stream()
            .filter(node -> node != null && node.getName() != null)
            .map(node -> "refs/heads/" + node.getName())
            .toList();
    }

    /** 저장소 정보를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        private Refs refs;
    }

    /** 브랜치 refs 목록을 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Refs {
        private List<Node> nodes;
    }

    /** 브랜치 노드를 담는 내부 DTO. */
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Node {
        private String name;
    }
}
