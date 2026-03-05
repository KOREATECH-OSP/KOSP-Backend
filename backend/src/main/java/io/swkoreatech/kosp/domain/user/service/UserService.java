package io.swkoreatech.kosp.domain.user.service;

import io.swkoreatech.kosp.common.auth.model.Role;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.auth.dto.response.AuthTokenResponse;
import io.swkoreatech.kosp.domain.auth.dto.response.CheckMemberIdResponse;
import io.swkoreatech.kosp.domain.auth.repository.RoleRepository;
import io.swkoreatech.kosp.domain.auth.service.AuthService;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitApplyRepository;
import io.swkoreatech.kosp.domain.github.repository.GithubUserRepository;
import io.swkoreatech.kosp.domain.mail.service.EmailVerificationService;
import io.swkoreatech.kosp.domain.point.model.PointTransaction;
import io.swkoreatech.kosp.domain.point.repository.PointTransactionRepository;
import io.swkoreatech.kosp.domain.user.dto.request.UserSignupRequest;
import io.swkoreatech.kosp.domain.user.dto.request.UserUpdateRequest;
import io.swkoreatech.kosp.domain.user.dto.response.MyApplicationListResponse;
import io.swkoreatech.kosp.domain.user.dto.response.MyPointHistoryResponse;
import io.swkoreatech.kosp.domain.user.dto.response.UserProfileResponse;
import io.swkoreatech.kosp.domain.user.event.UserSignupEvent;
import io.swkoreatech.kosp.global.auth.token.SignupToken;
import io.swkoreatech.kosp.global.util.RsqlUtils;

import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 서비스.
 * 회원가입, 정보 수정, 프로필 조회, 탈퇴, 비밀번호 변경, 지원 내역/포인트 내역 조회 기능을 담당한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final GithubUserRepository githubUserRepository;
    private final RecruitApplyRepository recruitApplyRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthService authService;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailVerificationService emailVerificationService;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 회원가입을 처리하고 인증 토큰을 발급한다.
     * 이메일 인증 확인, GitHub 정보 연동, 사용자 생성/복구, 권한 할당 및 토큰 발급을 수행한다.
     *
     * @param request 회원가입 요청
     * @param token 회원가입 토큰 (이메일/GitHub 인증 정보 포함)
     * @return 인증 토큰 응답
     * @throws GlobalException 이메일 미인증 또는 이미 가입된 사용자인 경우
     */
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
                .build());
        
        // GitHub 정보 업데이트 (암호화된 토큰 그대로 저장)
        githubUser.updateProfile(githubLogin, githubName, githubAvatarUrl, encryptedGithubToken);
        githubUserRepository.save(githubUser);

        // 4. 기존 유저 확인
        Optional<User> existingUser = userRepository.findByKutEmail(kutEmail);

        if (existingUser.isPresent() && !existingUser.get().isDeleted()) {
            throw new GlobalException(ExceptionMessage.USER_ALREADY_EXISTS);
        }

        User user = createOrReactivateUser(existingUser, request, kutEmail);

        user.updateGithubUser(githubUser);
        userRepository.save(user);

        // 5. 기본 권한 할당
        Role role = roleRepository.findByName("ROLE_STUDENT")
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ROLE_NOT_FOUND));
        user.getRoles().add(role);

        log.info("사용자 생성/복구 완료: userId={}, kutEmail={}", user.getId(), user.getKutEmail());
        
         // 6. GitHub 데이터 수집 이벤트 발행
          if (githubUser.getGithubLogin() != null) {
              eventPublisher.publishEvent(new UserSignupEvent(this, user.getId(), githubUser.getGithubLogin()));
              log.info("Published UserSignupEvent for user {} (GitHub: {})", user.getId(), githubUser.getGithubLogin());
           }
           
           emailVerificationService.completeSignupVerification(kutEmail);
           log.info("Redis cleanup completed for email: {}", kutEmail);
          return authService.createTokensForUser(user);
    }

    /**
     * 사용자 정보를 수정한다.
     *
     * @param userId 사용자 ID
     * @param request 수정 요청
     */
    @Transactional
    public void update(Long userId, UserUpdateRequest request) {
        User user = userRepository.getById(userId);
        user.updateInfo(request.name(), request.introduction());
    }

    /**
     * 사용자 프로필을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 사용자 프로필 응답
     */
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.getById(userId);
        return UserProfileResponse.from(user);
    }
    /**
     * 사용자 계정을 탈퇴(Soft Delete) 처리한다.
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void delete(Long userId) {
        User user = userRepository.getById(userId);
        user.delete();
    }

    /**
     * 사용자의 비밀번호를 변경한다.
     *
     * @param userId 사용자 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     * @throws GlobalException 현재 비밀번호가 일치하지 않는 경우
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.getById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new GlobalException(ExceptionMessage.AUTHENTICATION);
        }

        user.changePassword(newPassword, passwordEncoder);
    }
    /**
     * 학번/사번의 사용 가능 여부를 확인한다.
     *
     * @param memberId 학번 또는 사번
     * @return 사용 가능 여부 응답
     */
    @Transactional(readOnly = true)
    public CheckMemberIdResponse checkMemberIdAvailability(String memberId) {
        boolean exists = userRepository.existsByKutIdAndIsDeletedFalse(memberId);
        String label = extractMemberLabel(memberId);
        String message = buildAvailabilityMessage(exists, label);
        
        return CheckMemberIdResponse.from(!exists, message);
    }

    private String extractMemberLabel(String memberId) {
        if (memberId.length() == 10) {
            return "학번";
        }
        return "사번";
    }

    private String buildAvailabilityMessage(boolean exists, String label) {
        if (exists) {
            return "이미 가입된 " + label + "입니다.";
        }
        return "사용 가능한 " + label + "입니다.";
    }

    /**
     * 로그인한 사용자의 지원 내역을 RSQL 필터로 조회한다.
     *
     * @param user 인증된 사용자
     * @param filter RSQL 필터 문자열
     * @param pageable 페이지 정보
     * @return 지원 내역 목록 응답
     */
    public MyApplicationListResponse getMyApplications(User user, String filter, Pageable pageable) {
        Specification<RecruitApply> baseSpec = (root, query, cb) -> cb.equal(root.get("user"), user);
        Specification<RecruitApply> spec = RsqlUtils.toSpecification(filter, baseSpec);
        Page<RecruitApply> page = recruitApplyRepository.findAll(spec, pageable);
        return MyApplicationListResponse.from(page);
    }

    /**
     * 로그인한 사용자의 전체 지원 내역을 조회한다.
     *
     * @param user 인증된 사용자
     * @param pageable 페이지 정보
     * @return 지원 내역 목록 응답
     */
    public MyApplicationListResponse getMyApplications(User user, Pageable pageable) {
        return getMyApplications(user, null, pageable);
    }

    /**
     * 로그인한 사용자의 포인트 내역을 조회한다.
     *
     * @param user 인증된 사용자
     * @param pageable 페이지 정보
     * @return 포인트 내역 응답
     */
    public MyPointHistoryResponse getMyPointHistory(User user, Pageable pageable) {
        Page<PointTransaction> transactions = pointTransactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return MyPointHistoryResponse.from(user, transactions);
    }

    private User createOrReactivateUser(Optional<User> existingUser, UserSignupRequest request, String kutEmail) {
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.reactivate();
            user.changePassword(request.password(), passwordEncoder);
            return user;
        }

        User user = User.builder()
            .name(request.name())
            .kutId(request.kutId())
            .kutEmail(kutEmail)
            .password(request.password())
            .build();

        user.encodePassword(passwordEncoder);
        return user;
    }
}
