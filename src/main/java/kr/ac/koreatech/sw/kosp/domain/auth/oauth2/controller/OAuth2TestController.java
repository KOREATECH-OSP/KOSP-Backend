package kr.ac.koreatech.sw.kosp.domain.auth.oauth2.controller;

import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotNull;

@Profile({"dev", "local"})
@RestController
public class OAuth2TestController {

    @GetMapping("/test/oauth2/callback")
    public ResponseEntity<Map<String, Object>> testCallback(
        @RequestParam @NotNull boolean isNew,
        @RequestParam @NotNull Long githubId
    ) {
        return ResponseEntity.ok(Map.of(
            "isNew", isNew,
            "githubId", githubId,
            "message", "This is a temporary endpoint for testing OAuth2 redirect."
        ));
    }
}
