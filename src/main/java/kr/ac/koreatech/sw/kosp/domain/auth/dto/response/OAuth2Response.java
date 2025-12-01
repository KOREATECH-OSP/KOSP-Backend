package kr.ac.koreatech.sw.kosp.domain.auth.dto.response;

public record OAuth2Response(
    boolean isNew,
    Long githubId
) {
}
