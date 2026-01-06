package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitRaw;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GithubCommitRawRepository extends ReactiveMongoRepository<GithubCommitRaw, String> {
    
    Flux<GithubCommitRaw> findByRepoOwnerAndRepoName(String repoOwner, String repoName);
    
    Mono<GithubCommitRaw> findByRepoOwnerAndRepoNameAndSha(
        String repoOwner,
        String repoName,
        String sha
    );
    
    Mono<Void> deleteByRepoOwnerAndRepoName(String repoOwner, String repoName);
}
