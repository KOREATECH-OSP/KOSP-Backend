package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubUserFollowing;

public interface GithubUserFollowingRepository extends MongoRepository<GithubUserFollowing, String> {
    
    List<GithubUserFollowing> findByGithubId(String githubId);
    
    long countByGithubId(String githubId);
    
    boolean existsByGithubIdAndFollowingId(String githubId, String followingId);
}
