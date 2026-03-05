package io.swkoreatech.kosp.domain.admin.member.service;

import io.swkoreatech.kosp.common.auth.model.Role;
import io.swkoreatech.kosp.common.event.GithubCollectionRequest;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.admin.member.dto.request.AdminUserUpdateRequest;
import io.swkoreatech.kosp.domain.admin.member.dto.response.AdminUserListResponse;
import io.swkoreatech.kosp.domain.auth.repository.RoleRepository;
import io.swkoreatech.kosp.domain.user.event.UserSignupEvent;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 사용자 관리 서비스.
 * <p>사용자 역할 변경, 강제 탈퇴, 정보 수정, GitHub 수집 트리거 등의 비즈니스 로직을 처리한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 사용자의 역할을 변경한다.
     *
     * @param userId    사용자 식별자
     * @param roleNames 변경할 역할 이름 집합
     * @throws GlobalException 역할을 찾을 수 없는 경우
     */
    @Transactional
    public void updateUserRoles(Long userId, Set<String> roleNames) {
        User user = userRepository.getById(userId);
        
        Set<Role> roles = roleNames.stream()
            .map(this::findRole)
            .collect(Collectors.toSet());
            
        user.getRoles().clear();
        user.getRoles().addAll(roles);

    }

    /**
     * 사용자를 강제 탈퇴(소프트 삭제) 처리한다.
     *
     * @param userId 사용자 식별자
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.getById(userId);
        
        user.delete();
    }

    private Role findRole(String name) {
        return roleRepository.findByName(name)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }

    /**
     * 사용자 목록을 페이지네이션하여 조회한다.
     *
     * @param pageable 페이지 정보
     * @return 관리자 사용자 목록 응답 DTO
     */
    public AdminUserListResponse getUsers(org.springframework.data.domain.Pageable pageable) {
        return AdminUserListResponse.from(userRepository.findAll(pageable));
    }


    /**
     * 사용자 정보를 관리자 권한으로 수정한다.
     *
     * @param userId  사용자 식별자
     * @param request 수정 요청 DTO
     * @throws GlobalException 학번/사번 또는 이메일 중복 시
     */
    @Transactional
    public void updateUser(Long userId, AdminUserUpdateRequest request) {
        User user = userRepository.getById(userId);

        if (request.kutId() != null) {
            boolean kutIdExists = userRepository.existsByKutIdAndIdNot(request.kutId(), userId);
            if (kutIdExists) {
                throw new GlobalException(ExceptionMessage.USER_ALREADY_EXISTS);
            }
        }

        if (request.kutEmail() != null) {
            String normalizedEmail = request.kutEmail().toLowerCase();
            boolean kutEmailExists = userRepository.existsByKutEmailAndIdNot(normalizedEmail, userId);
            if (kutEmailExists) {
                throw new GlobalException(ExceptionMessage.USER_ALREADY_EXISTS);
            }
        }

        user.updateInfo(request.name(), request.introduction());

        if (request.kutId() != null) {
            user.updateKutId(request.kutId());
        }
        if (request.kutEmail() != null) {
            user.updateKutEmail(request.kutEmail());
        }

        if (request.profileImageUrl() != null && user.getGithubUser() != null) {
            user.getGithubUser().updateAvatarUrl(request.profileImageUrl());
        }
    }

    /**
     * 특정 사용자의 GitHub 데이터 수집을 수동으로 트리거한다.
     *
     * @param userId 사용자 식별자
     * @throws GlobalException GitHub 계정이 연동되지 않은 경우
     */
    @Transactional
    public void triggerGithubCollection(Long userId) {
        User user = userRepository.getById(userId);

        if (user.getGithubUser() == null) {
            throw new GlobalException(ExceptionMessage.GITHUB_USER_NOT_FOUND);
        }

        String githubLogin = user.getGithubUser().getGithubLogin();
        eventPublisher.publishEvent(new UserSignupEvent(this, userId, githubLogin));
        log.info("Triggered GitHub collection for user {} (GitHub: {})", userId, githubLogin);
    }

    /**
     * 모든 활성 사용자의 GitHub 데이터 수집을 수동으로 트리거한다.
     */
    public void triggerAllGithubCollection() {
        List<Long> userIds = userRepository.findActiveUserIds();
        userIds.forEach(this::publishCollectionRequest);
        log.info("Triggered GitHub collection for {} users", userIds.size());
    }

    private void publishCollectionRequest(Long userId) {
        GithubCollectionRequest dto = new GithubCollectionRequest(userId);
        rabbitTemplate.convertAndSend(
            QueueNames.GITHUB_COLLECTION_EXCHANGE,
            QueueNames.GITHUB_COLLECTION,
            dto,
            message -> {
                message.getMessageProperties().setHeader("x-delay", 0);
                return message;
            }
        );
    }

}
