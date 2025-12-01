package kr.ac.koreatech.sw.kosp.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.api.AuthApi;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthMeResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.OAuth2Response;
import kr.ac.koreatech.sw.kosp.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<Void> login(
        LoginRequest request,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        authService.login(request, servletRequest, servletResponse);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<AuthMeResponse> getMyInfo() {
        return ResponseEntity.ok(authService.getUserInfo());
    }

    @Override
    public ResponseEntity<OAuth2Response> oAuth2ResultHandler(HttpServletRequest request) {
        boolean isNew = (boolean)request.getAttribute("isNew");
        Long githubId = (Long)request.getAttribute("githubId");

        // TODO githubId로 User를 조회한 다음, 존재하는지 확인. 없으면 isNew는 true이고, 있으면 false임.

        return ResponseEntity.ok(new OAuth2Response(isNew, githubId));
    }

}
