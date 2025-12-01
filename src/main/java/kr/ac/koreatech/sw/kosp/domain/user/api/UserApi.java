package kr.ac.koreatech.sw.kosp.domain.user.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;

@Tag(
    name = "User",
    description = "사용자 계정(User) 도메인과 관련된 API 모음임. 회원가입과 같이 사용자 엔티티를 생성·관리하는 기능을 제공함."
)
@RequestMapping("/v1/users")
public interface UserApi {

    @PostMapping("/signup")
    @Operation(
        summary = "회원가입 및 즉시 로그인",
        description = """
            새 사용자 정보를 전달받아 내부적으로 User 엔티티를 생성하고 저장한 뒤,
            인증 모듈(AuthService)을 통해 즉시 로그인 처리까지 수행함.
            클라이언트는 이 API 한 번 호출로 회원가입과 세션/토큰 발급 과정을 한 번에 수행할 수 있음.
            """
    )
    ResponseEntity<Void> signup(
        @RequestBody @Valid UserSignupRequest request,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    );
}


