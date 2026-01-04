package kr.ac.koreatech.sw.kosp.domain.auth.oauth2.dto.response;

import org.springframework.web.util.UriComponentsBuilder;

public record OAuth2Response(boolean isNew, Long githubId) {

    public static OAuth2Response of(boolean isNew, Long githubId) {
        return new OAuth2Response(isNew, githubId);
    }

    public String appendQueryParams(String url) {
        return UriComponentsBuilder.fromUriString(url)
                .queryParam("isNew", isNew)
                .queryParam("githubId", githubId)
                .build()
                .toUriString();
    }
}
