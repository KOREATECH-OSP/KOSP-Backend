package kr.ac.koreatech.sw.kosp.domain.admin.service;

import java.util.Set;
import java.util.stream.Collectors;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.domain.auth.service.PermissionAdminService;
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
public class AdminMemberService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionAdminService permissionAdminService;

    @Transactional
    public void updateUserRoles(Long userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
        
        Set<Role> roles = roleNames.stream()
            .map(this::findRole)
            .collect(Collectors.toSet());
            
        // User.roles is Set<Role>. We need to update it.
        // User entity should expose method to update roles? 
        // Direct access via getter if modifiable collection works inside transaction.
        user.getRoles().clear();
        user.getRoles().addAll(roles);

        permissionAdminService.publishPermissionChange(user.getKutEmail());
    }

    private Role findRole(String name) {
        return roleRepository.findByName(name)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }
}
