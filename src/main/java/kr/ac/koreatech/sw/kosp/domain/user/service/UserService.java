package kr.ac.koreatech.sw.kosp.domain.user.service;

import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.user.dto.response.UserProfileResponse;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final GithubUserRepository githubUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final kr.ac.koreatech.sw.kosp.domain.mail.service.EmailVerificationService emailVerificationService;

    @Transactional
    public void signup(UserSignupRequest request) {
        // 이메일 인증 확인 (인증 안된 경우 예외 발생)
        emailVerificationService.completeSignupVerification(request.kutEmail());

        GithubUser githubUser = githubUserRepository.getByGithubId(request.githubId());

        // 기존 유저 확인 (탈퇴 회원 포함)
        Optional<User> existingUser = userRepository.findByKutEmail(request.kutEmail());

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (!user.isDeleted()) {
                throw new GlobalException(ExceptionMessage.USER_ALREADY_EXISTS);
            }
            // 탈퇴 계정 복구 (Reactivation)
            user.reactivate(); // isDeleted=false, roles.clear()
            user.changePassword(request.password(), passwordEncoder);
            user.updateGithubUser(githubUser);
        } else {
            // 신규 생성
            user = request.toUser();
            user.encodePassword(passwordEncoder);
            user.updateGithubUser(githubUser);
            userRepository.save(user); // 신규 저장은 save 필요, 기존 유저는 dirty checking
        }

        // 기본 권한 할당 (학생)
        Role role = roleRepository.findByName("ROLE_STUDENT")
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ROLE_NOT_FOUND));
        user.getRoles().add(role);

        log.info("✅ 사용자 생성/복구 완료: userId={}, kutEmail={}", user.getId(), user.getKutEmail());
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
}
