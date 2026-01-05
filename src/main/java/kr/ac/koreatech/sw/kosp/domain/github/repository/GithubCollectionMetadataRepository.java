package kr.ac.koreatech.sw.kosp.domain.github.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubCollectionMetadata;

public interface GithubCollectionMetadataRepository extends Repository<GithubCollectionMetadata, Long> {

    GithubCollectionMetadata save(GithubCollectionMetadata metadata);

    Optional<GithubCollectionMetadata> findByGithubId(String githubId);

    List<GithubCollectionMetadata> findByInitialCollectedFalse();

    boolean existsByGithubId(String githubId);
}
