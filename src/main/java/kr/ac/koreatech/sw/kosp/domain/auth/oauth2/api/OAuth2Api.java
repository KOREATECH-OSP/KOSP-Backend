package kr.ac.koreatech.sw.kosp.domain.auth.oauth2.api;

import java.io.IOException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface OAuth2Api {

    @Hidden
    @RequestMapping(value = "/result", method = {RequestMethod.GET, RequestMethod.POST})
    void oAuth2ResultHandler(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException;

}
