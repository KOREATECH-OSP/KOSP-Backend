package kr.ac.koreatech.sw.kosp.domain.admin.role.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.RoleRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.RoleUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.RoleResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.PolicyRepository;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleAdminService {

    private static final String SUPERUSER_ROLE = "ROLE_SUPERUSER";
    
    private final RoleRepository roleRepository;
    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
            .map(RoleResponse::from)
            .toList();
    }

    public RoleResponse getRole(String name) {
        return RoleResponse.from(roleRepository.getByName(name));
    }

    @Transactional
    public void createRole(RoleRequest request) {
        if (roleRepository.existsByName(request.name())) {
            throw new GlobalException(ExceptionMessage.CONFLICT);
        }
        Boolean canAccessAdmin = request.canAccessAdmin() != null ? request.canAccessAdmin() : false;
        roleRepository.save(
            Role.builder()
                .name(request.name())
                .description(request.description())
                .canAccessAdmin(canAccessAdmin)
                .build()
        );
    }

    @Transactional
    public void updateRole(String name, RoleUpdateRequest request) {
        validateNotSuperuser(name);
        Role role = roleRepository.getByName(name);
        role.updateDescription(request.description());
        role.updateCanAccessAdmin(request.canAccessAdmin());
    }

    @Transactional
    public void deleteRole(String name) {
        validateNotSuperuser(name);
        
        // Verify role exists before deletion
        roleRepository.getByName(name);
        
        // Check if any users have this role
        if (userRepository.existsByRoles_Name(name)) {
            throw new GlobalException(ExceptionMessage.CONFLICT);
        }
        
        roleRepository.deleteByName(name);
    }

    @Transactional
    public void assignPolicy(String roleName, String policyName) {
        validateNotSuperuser(roleName);
        
        Role role = roleRepository.getByName(roleName);
        Policy policy = policyRepository.getByName(policyName);

        role.getPolicies().add(policy);
    }

    @Transactional
    public void removePolicy(String roleName, String policyName) {
        validateNotSuperuser(roleName);
        
        Role role = roleRepository.getByName(roleName);
        Policy policy = policyRepository.getByName(policyName);
        
        role.getPolicies().remove(policy);
    }
    
    private void validateNotSuperuser(String roleName) {
        if (SUPERUSER_ROLE.equals(roleName)) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }

}
