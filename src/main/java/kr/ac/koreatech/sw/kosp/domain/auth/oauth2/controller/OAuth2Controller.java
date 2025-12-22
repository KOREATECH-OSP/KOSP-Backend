package kr.ac.koreatech.sw.kosp.domain.auth.oauth2.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.oauth2.api.OAuth2Api;
import kr.ac.koreatech.sw.kosp.domain.auth.oauth2.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller implements OAuth2Api {

    private final OAuth2UserService oAuth2UserService;

    @Override
    @RequestMapping(value = "/result", method = {RequestMethod.GET, RequestMethod.POST})
    public void oAuth2ResultHandler(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {

        String redirectUrl = oAuth2UserService.oAuth2ResultHandler(request, response);
        response.sendRedirect(redirectUrl);
    }
}
