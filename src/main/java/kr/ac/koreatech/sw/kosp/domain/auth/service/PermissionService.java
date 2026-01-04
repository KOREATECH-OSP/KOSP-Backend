package kr.ac.koreatech.sw.kosp.domain.auth.service;

import kr.ac.koreatech.sw.kosp.domain.auth.model.Permission;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionService {

    private final UserRepository userRepository;

    public boolean hasPermission(Long userId, String permissionName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));

        // Traverse: User -> Role -> Policy -> Permission
        // Since we are in @Transactional, Lazy Loading is safe.
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
