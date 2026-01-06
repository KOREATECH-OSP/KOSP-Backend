package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubIssueRaw;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GithubIssueRawRepository extends ReactiveMongoRepository<GithubIssueRaw, String> {
    
    Flux<GithubIssueRaw> findByRepoOwnerAndRepoName(String repoOwner, String repoName);
    
    Mono<GithubIssueRaw> findByRepoOwnerAndRepoNameAndIssueNumber(
        String repoOwner,
        String repoName,
        Integer issueNumber
    );
    
    Mono<Void> deleteByRepoOwnerAndRepoName(String repoOwner, String repoName);
}
