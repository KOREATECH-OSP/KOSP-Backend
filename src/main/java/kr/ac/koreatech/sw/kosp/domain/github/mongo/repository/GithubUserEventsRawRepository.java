package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubUserEventsRaw;
import reactor.core.publisher.Mono;

public interface GithubUserEventsRawRepository 
    extends ReactiveMongoRepository<GithubUserEventsRaw, String> {
    
    Mono<GithubUserEventsRaw> findByGithubLogin(String githubLogin);
}
