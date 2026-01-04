package kr.ac.koreatech.sw.kosp.domain.github.dto;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubProfile;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubRepository;

public record UserSyncResult(
    GithubProfile profile,
    List<GithubRepository> repositories
) {}
