package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubPRRaw;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GithubPRRawRepository extends ReactiveMongoRepository<GithubPRRaw, String> {
    
    Flux<GithubPRRaw> findByRepoOwnerAndRepoName(String repoOwner, String repoName);
    
    Mono<GithubPRRaw> findByRepoOwnerAndRepoNameAndPrNumber(
        String repoOwner,
        String repoName,
        Integer prNumber
    );
    
    Mono<Void> deleteByRepoOwnerAndRepoName(String repoOwner, String repoName);

    @org.springframework.data.mongodb.repository.Query("{ 'prData.user.login': ?0 }")
    Flux<GithubPRRaw> findByAuthorLogin(String login);
}
