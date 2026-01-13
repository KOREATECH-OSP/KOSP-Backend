package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubRepoContribute;

public interface GithubRepoContributeRepository extends MongoRepository<GithubRepoContribute, String> {
    
    List<GithubRepoContribute> findByGithubId(String githubId);
    
    List<GithubRepoContribute> findByGithubIdAndIsOwned(String githubId, Boolean isOwned);
    
    boolean existsByGithubIdAndOwnerIdAndRepoName(String githubId, String ownerId, String repoName);
}
