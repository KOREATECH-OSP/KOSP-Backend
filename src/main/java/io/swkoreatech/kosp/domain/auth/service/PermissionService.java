package io.swkoreatech.kosp.domain.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.auth.model.Permission;
import io.swkoreatech.kosp.domain.auth.model.Policy;
import io.swkoreatech.kosp.domain.auth.model.Role;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionService {

    private final UserRepository userRepository;

    public boolean hasPermission(Long userId, String permissionName) {
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));

        // Traverse: User -> Role -> Policy -> Permission
        // All associations are eagerly fetched via JOIN FETCH
        for (Role role : user.getRoles()) {
            for (Policy policy : role.getPolicies()) {
                for (Permission permission : policy.getPermissions()) {
                    if (permission.getName().equals(permissionName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
