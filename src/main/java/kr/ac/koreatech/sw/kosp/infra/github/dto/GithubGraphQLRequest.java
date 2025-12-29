package kr.ac.koreatech.sw.kosp.infra.github.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GithubGraphQLRequest {
    private String query;

    public GithubGraphQLRequest(String query) {
        this.query = query;
    }
}
