package kr.ac.koreatech.sw.kosp.domain.github.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubActivity;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;

public interface GithubActivityRepository extends Repository<GithubActivity, Long> {
    GithubActivity save(GithubActivity githubActivity);
    Optional<GithubActivity> findByUser(User user);
}
