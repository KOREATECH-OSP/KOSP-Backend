package kr.ac.koreatech.sw.kosp.domain.user.service;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthTokenResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.user.dto.response.UserProfileResponse;
import kr.ac.koreatech.sw.kosp.domain.user.event.UserSignupEvent;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.auth.token.SignupToken;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final GithubUserRepository githubUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final kr.ac.koreatech.sw.kosp.domain.auth.service.AuthService authService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AuthTokenResponse signup(UserSignupRequest request, SignupToken token) {
        // 1. Email Verified 확인
        if (!token.isEmailVerified()) {
            throw new GlobalException(ExceptionMessage.EMAIL_NOT_VERIFIED);
        }

        String kutEmail = token.getKutEmail();
        Long githubId = Long.valueOf(token.getGithubId());
        String encryptedGithubToken = token.getEncryptedGithubToken();

        // 2. GitHub 정보 추출
        String githubLogin = token.getLogin();
        String githubName = token.getName();
        String githubAvatarUrl = token.getAvatarUrl();

        // 3. GithubUser 조회 또는 생성
        GithubUser githubUser = githubUserRepository.findByGithubId(githubId)
            .orElseGet(() -> GithubUser.builder()
                .githubId(githubId)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build());
        
        // GitHub 정보 업데이트 (암호화된 토큰 그대로 저장)
        githubUser.updateProfile(githubLogin, githubName, githubAvatarUrl, encryptedGithubToken);
        githubUserRepository.save(githubUser);

        // 4. 기존 유저 확인
        Optional<User> existingUser = userRepository.findByKutEmail(kutEmail);

        if (existingUser.isPresent() && !existingUser.get().isDeleted()) {
            throw new GlobalException(ExceptionMessage.USER_ALREADY_EXISTS);
        }

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.reactivate(); 
            user.changePassword(request.password(), passwordEncoder);
        } else {
            // 신규 생성
            user = User.builder()
                .name(request.name())
                .kutId(request.kutId())
                .kutEmail(kutEmail)
                .password(request.password())
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
                
            user.encodePassword(passwordEncoder);
        }

        user.updateGithubUser(githubUser);
        userRepository.save(user);

        // 5. 기본 권한 할당
        Role role = roleRepository.findByName("ROLE_STUDENT")
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ROLE_NOT_FOUND));
        user.getRoles().add(role);

        log.info("✅ 사용자 생성/복구 완료: userId={}, kutEmail={}", user.getId(), user.getKutEmail());
        
        // 6. GitHub 데이터 수집 이벤트 발행
        if (githubUser.getGithubLogin() != null) {
            eventPublisher.publishEvent(new UserSignupEvent(this, githubUser.getGithubLogin()));
            log.info("Published UserSignupEvent for GitHub user: {}", githubUser.getGithubLogin());
        }
        
        return authService.createTokensForUser(user);
    }

    @Transactional
    public void update(Long userId, UserUpdateRequest req) {
        User user = userRepository.getById(userId);
        user.updateInfo(req.name(), req.introduction());
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.getById(userId);
        return UserProfileResponse.from(user);
    }
    @Transactional
    public void delete(Long userId) {
        User user = userRepository.getById(userId);
        user.delete();
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.getById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new GlobalException(ExceptionMessage.AUTHENTICATION);
        }

        user.changePassword(newPassword, passwordEncoder);
    }
    @Transactional(readOnly = true)
    public kr.ac.koreatech.sw.kosp.domain.auth.dto.response.CheckMemberIdResponse checkMemberIdAvailability(String memberId) {
        // 중복 확인 (탈퇴한 사용자 제외)
        boolean exists = userRepository.existsByKutIdAndIsDeletedFalse(memberId);
        
        // 10자리는 학번(STUDENT), 그 외는 사번(STAFF)으로 간주 (이미 DTO에서 포맷 검증됨)
        String label = (memberId.length() == 10) ? "학번" : "사번";
        
        return new kr.ac.koreatech.sw.kosp.domain.auth.dto.response.CheckMemberIdResponse(
            true, 
            !exists, 
            exists ? "이미 가입된 " + label + "입니다." : "사용 가능한 " + label + "입니다."
        );
    }
}
