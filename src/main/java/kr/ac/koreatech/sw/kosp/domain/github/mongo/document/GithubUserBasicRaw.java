package kr.ac.koreatech.sw.kosp.domain.github.mongo.document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Document(collection = "github_user_basic_raw")
@Getter
@Builder
public class GithubUserBasicRaw {

    @Id
    private String id;

    private String githubId;  // login
    private String name;
    private String avatarUrl;
    private String bio;
    private String company;
    private String location;
    private String email;
    private String createdAt;

    // 팔로워/팔로잉
    private Integer followersCount;
    private Integer followingCount;

    // 레포지토리 정보
    private Integer totalRepositories;
    private List<Map<String, Object>> repositories;

    // 기여 통계
    private Map<String, Object> contributionsCollection;

    // 메타데이터
    private LocalDateTime collectedAt;

    public static GithubUserBasicRaw create(
        String githubId,
        String name,
        String avatarUrl,
        String bio,
        String company,
        String location,
        String email,
        String createdAt,
        Integer followersCount,
        Integer followingCount,
        Integer totalRepositories,
        List<Map<String, Object>> repositories,
        Map<String, Object> contributionsCollection
    ) {
        return GithubUserBasicRaw.builder()
            .githubId(githubId)
            .name(name)
            .avatarUrl(avatarUrl)
            .bio(bio)
            .company(company)
            .location(location)
            .email(email)
            .createdAt(createdAt)
            .followersCount(followersCount)
            .followingCount(followingCount)
            .totalRepositories(totalRepositories)
            .repositories(repositories)
            .contributionsCollection(contributionsCollection)
            .collectedAt(LocalDateTime.now())
            .build();
    }
}
