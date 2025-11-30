package kr.ac.koreatech.sw.kosp.domain.auth.service;

import static kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.AUTHENTICATION;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.LoginResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.jwt.model.JwtToken;
import kr.ac.koreatech.sw.kosp.domain.auth.jwt.repository.JwtTokenRedisRepository;
import kr.ac.koreatech.sw.kosp.domain.auth.jwt.service.JwtService;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.UserSignInRequest;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtTokenRedisRepository jwtTokenRedisRepository;

    /**
     * 일반 로그인 (이메일 + 비밀번호)
     */
    @Transactional
    public JwtToken login(UserSignInRequest request) {
        User user = userRepository.getByKutEmail(request.email());

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("❌ 비밀번호 불일치: email={}", request.email());
            throw new GlobalException(AUTHENTICATION.getMessage(), AUTHENTICATION.getStatus());
        }

        // 삭제된 사용자 체크
        if (user.isDeleted()) {
            log.warn("❌ 삭제된 사용자: userId={}", user.getId());
            throw new GlobalException(AUTHENTICATION.getMessage(), AUTHENTICATION.getStatus());
        }

        log.info("✅ 로그인 성공: userId={}, email={}", user.getId(), user.getKutEmail());
        return jwtService.createJwtToken(user.getId());
    }

    /**
     * 사용자 정보 조회
     */
    public LoginResponse getUserInfo(Integer userId) {
        User user = userRepository.getById(userId);

        return new LoginResponse(
            user.getKutEmail()
        );
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(Integer userId) {
        jwtTokenRedisRepository.deleteById(userId);
        log.info("✅ 로그아웃 성공: userId={}", userId);
    }
}
