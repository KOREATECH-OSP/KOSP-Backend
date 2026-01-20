package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubRepository;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface GithubRepositoryRepository extends Repository<GithubRepository, String> {
    GithubRepository save(GithubRepository repository);
    List<GithubRepository> findByOwnerIdOrderByCodeVolumeTotalCommitsDesc(Long ownerId);
    List<GithubRepository> findByNameContainingIgnoreCase(String name);
    List<GithubRepository> findByDescriptionContainingIgnoreCase(String description);
}
