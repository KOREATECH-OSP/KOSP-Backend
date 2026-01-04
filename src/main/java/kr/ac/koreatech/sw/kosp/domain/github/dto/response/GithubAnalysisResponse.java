package kr.ac.koreatech.sw.kosp.domain.github.dto.response;

import java.util.Map;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubProfile;

public record GithubAnalysisResponse(
    Long githubId,
    String bio,
    Integer tier,
    Integer followers,
    Integer following,
    GithubProfile.Stats stats,
    GithubProfile.Analysis analysis,
    Map<String, Long> languageStats
) {
    public static GithubAnalysisResponse from(GithubProfile profile) {
        return new GithubAnalysisResponse(
            profile.getGithubId(),
            profile.getBio(),
            profile.getTier(),
            profile.getFollowers(),
            profile.getFollowing(),
            profile.getStats(),
            profile.getAnalysis(),
            profile.getLanguageStats()
        );
    }
}
