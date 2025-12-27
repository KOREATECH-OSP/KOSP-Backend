package kr.ac.koreatech.sw.kosp.domain.auth.oauth2.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.oauth2.api.OAuth2Api;
import kr.ac.koreatech.sw.kosp.domain.auth.oauth2.service.OAuth2LoginService;
import lombok.RequiredArgsConstructor;

import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;

@RestController
@RequestMapping("/v1/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller implements OAuth2Api {

    private final OAuth2LoginService oAuth2LoginService;

    @Override
    @RequestMapping(value = "/result", method = {RequestMethod.GET, RequestMethod.POST})
    @Permit(permitAll = true, description = "OAuth2 로그인 처리")
    public void oAuth2ResultHandler(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {

        String redirectUrl = oAuth2LoginService.oAuth2ResultHandler(request, response);
        response.sendRedirect(redirectUrl);
    }
}
