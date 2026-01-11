package kr.ac.koreatech.sw.kosp.domain.admin.member.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.admin.member.dto.request.AdminUserUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

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

    public kr.ac.koreatech.sw.kosp.domain.admin.member.dto.response.AdminUserListResponse getUsers(org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<User> userPage = userRepository.findAll(pageable);
        
        java.util.List<kr.ac.koreatech.sw.kosp.domain.admin.member.dto.response.AdminUserListResponse.UserInfo> userInfos = userPage.getContent().stream()
            .map(user -> {
                String profileImageUrl = user.getGithubUser() != null 
                    ? user.getGithubUser().getGithubAvatarUrl() 
                    : null;
                
                Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
                
                return new kr.ac.koreatech.sw.kosp.domain.admin.member.dto.response.AdminUserListResponse.UserInfo(
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
        
        return new kr.ac.koreatech.sw.kosp.domain.admin.member.dto.response.AdminUserListResponse(
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

        // Update User info (name, introduction)
        user.updateInfo(request.name(), request.introduction());

        // Update GithubUser info (profileImage) if exists and requested
        if (request.profileImageUrl() != null && user.getGithubUser() != null) {
            user.getGithubUser().updateAvatarUrl(request.profileImageUrl());
        }
    }

}
