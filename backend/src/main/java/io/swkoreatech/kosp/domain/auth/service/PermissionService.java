package io.swkoreatech.kosp.domain.auth.service;

import io.swkoreatech.kosp.common.auth.model.Permission;
import io.swkoreatech.kosp.common.auth.model.Policy;
import io.swkoreatech.kosp.common.auth.model.Role;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 권한 검증 서비스.
 * <p>사용자의 역할-정책-권한 계층 구조를 탐색하여 특정 권한 보유 여부를 확인한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionService {

    private final UserRepository userRepository;

    /**
     * 사용자가 특정 권한을 보유하고 있는지 확인한다.
     *
     * @param userId         사용자 식별자
     * @param permissionName 확인할 권한 이름
     * @return 권한 보유 여부
     * @throws GlobalException 사용자를 찾을 수 없는 경우
     */
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
