package io.swkoreatech.kosp.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserBasicInfoResponse {
    private User user;
    
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String login;
        private String name;
        private String createdAt;
        private String updatedAt;
        private RepositoriesData repositories;
    }
    
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RepositoriesData {
        private int totalCount;
        private PageInfo pageInfo;
        private List<RepositoryNode> nodes;
    }
    
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageInfo {
        private boolean hasNextPage;
        private String endCursor;
    }
    
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RepositoryNode {
        private String name;
        private String nameWithOwner;
        private String description;
        private Owner owner;
        private boolean isFork;
        private boolean isPrivate;
        private PrimaryLanguage primaryLanguage;
        private int stargazerCount;
        private int forkCount;
        private Watchers watchers;
        private String createdAt;
        private String updatedAt;
    }
    
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner {
        private String login;
    }
    
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PrimaryLanguage {
        private String name;
    }
    
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Watchers {
        private int totalCount;
    }
}
