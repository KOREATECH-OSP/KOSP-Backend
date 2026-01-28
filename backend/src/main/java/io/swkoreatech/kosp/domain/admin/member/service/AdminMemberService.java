package io.swkoreatech.kosp.domain.admin.member.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.admin.member.dto.response.AdminUserListResponse;
import io.swkoreatech.kosp.domain.admin.member.dto.request.AdminUserUpdateRequest;
import io.swkoreatech.kosp.domain.auth.model.Role;
import io.swkoreatech.kosp.domain.auth.repository.RoleRepository;
import io.swkoreatech.kosp.domain.user.event.UserSignupEvent;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void updateUserRoles(Long userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
        
        Set<Role> roles = roleNames.stream()
            .map(this::findRole)
            .collect(Collectors.toSet());
            
        user.getRoles().clear();
        user.getRoles().addAll(roles);

    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
        
        user.delete();
    }

    private Role findRole(String name) {
        return roleRepository.findByName(name)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }

    public AdminUserListResponse getUsers(org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<User> userPage = userRepository.findAll(pageable);
        
        java.util.List<AdminUserListResponse.UserInfo> userInfos = userPage.getContent().stream()
            .map(user -> {
                String profileImageUrl = user.getGithubUser() != null 
                    ? user.getGithubUser().getGithubAvatarUrl() 
                    : null;
                
                Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
                
                return new AdminUserListResponse.UserInfo(
                    user.getId(),
                    user.getName(),
                    user.getKutEmail(),
                    user.getKutId(),
                    profileImageUrl,
                    user.getIntroduction(),
                    roleNames,
                    user.isDeleted(),
                    user.getCreatedAt()
                );
            })
            .toList();
        
        return new AdminUserListResponse(
            userInfos,
            userPage.getTotalElements(),
            userPage.getTotalPages(),
            userPage.getNumber(),
            userPage.getSize()
        );
    }


    @Transactional
    public void updateUser(Long userId, AdminUserUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));

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

    @Transactional
    public void triggerGithubCollection(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));

        if (user.getGithubUser() == null) {
            throw new GlobalException(ExceptionMessage.GITHUB_USER_NOT_FOUND);
        }

        String githubLogin = user.getGithubUser().getGithubLogin();
        eventPublisher.publishEvent(new UserSignupEvent(this, userId, githubLogin));
        log.info("Triggered GitHub collection for user {} (GitHub: {})", userId, githubLogin);
    }

}
