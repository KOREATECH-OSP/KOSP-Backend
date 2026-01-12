package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubUserStarred;

public interface GithubUserStarredRepository extends MongoRepository<GithubUserStarred, String> {
    
    List<GithubUserStarred> findByGithubId(String githubId);
    
    long countByGithubId(String githubId);
    
    boolean existsByGithubIdAndStarredRepoOwnerAndStarredRepoName(
        String githubId,
        String starredRepoOwner,
        String starredRepoName
    );
}
