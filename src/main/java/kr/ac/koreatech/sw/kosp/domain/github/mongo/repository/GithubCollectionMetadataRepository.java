package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCollectionMetadata;
import reactor.core.publisher.Mono;

public interface GithubCollectionMetadataRepository 
    extends ReactiveMongoRepository<GithubCollectionMetadata, String> {
    
    Mono<GithubCollectionMetadata> findByGithubLoginAndRepoOwnerAndRepoNameAndCollectionType(
        String githubLogin,
        String repoOwner,
        String repoName,
        String collectionType
    );
    
    Mono<GithubCollectionMetadata> findByGithubLoginAndCollectionType(
        String githubLogin,
        String collectionType
    );
    
    Mono<GithubCollectionMetadata> findByRepoOwnerAndRepoNameAndCollectionType(
        String repoOwner,
        String repoName,
        String collectionType
    );
}
