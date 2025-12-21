package kr.ac.koreatech.sw.kosp.domain.auth.oauth2.api;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface OAuth2Api {

    @RequestMapping("/result")
    ResponseEntity<Void> oAuth2ResultHandler(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException;

}
