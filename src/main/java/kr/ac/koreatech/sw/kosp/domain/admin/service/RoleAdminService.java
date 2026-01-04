package kr.ac.koreatech.sw.kosp.domain.admin.service;

import java.util.List;

import kr.ac.koreatech.sw.kosp.domain.admin.dto.request.RoleRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.dto.response.RoleResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.PolicyRepository;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;

import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleAdminService {

    private final RoleRepository roleRepository;
    private final PolicyRepository policyRepository;
    // private final PermissionAdminService permissionAdminService; // Unused for now

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
            .map(RoleResponse::from)
            .toList();
    }

    @Transactional
    public void createRole(RoleRequest request) {
        if (roleRepository.existsByName(request.name())) {
            throw new GlobalException(ExceptionMessage.CONFLICT);
        }
        roleRepository.save(
            Role.builder()
                .name(request.name())
                .description(request.description())
                .build()
        );
    }

    @Transactional
    public void assignPolicy(String roleName, String policyName) {
        Role role = findRole(roleName);
        Policy policy = findPolicy(policyName);

        role.getPolicies().add(policy);
        // Event publishing (refresh cache) could be global or per user. 
        // Changing role policy affects ALL users with that role.
        // PermissionAdminService.publishPermissionChange takes username.
        // We might need a global refresh or iterate users (expensive).
        // For now, assume global refresh needed or Redis TTL handles it.
        // Or send "ALL" as username?
    }

    private Role findRole(String name) {
        return roleRepository.findByName(name)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }

    private Policy findPolicy(String name) {
        return policyRepository.findByName(name)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND)); // Need POLICY_NOT_FOUND
    }
}
