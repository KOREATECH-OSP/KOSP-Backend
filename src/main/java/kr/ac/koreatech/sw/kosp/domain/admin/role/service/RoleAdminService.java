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
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

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

    public RoleResponse getRole(String name) {
        return RoleResponse.from(findRole(name));
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
    public void updateRole(String name, RoleUpdateRequest request) {
        Role role = findRole(name);
        role.updateDescription(request.description());
    }

    @Transactional
    public void deleteRole(String name) {
        Role role = findRole(name);
        // Check if any users have this role via repository query
        // For now, we'll skip this check as it requires UserRepository injection
        // TODO: Add UserRepository and check if any users have this role
        roleRepository.deleteByName(name);
    }

    @Transactional
    public void assignPolicy(String roleName, String policyName) {
        Role role = findRole(roleName);
        Policy policy = findPolicy(policyName);

        role.getPolicies().add(policy);
    }

    @Transactional
    public void removePolicy(String roleName, String policyName) {
        Role role = findRole(roleName);
        Policy policy = findPolicy(policyName);
        
        role.getPolicies().remove(policy);
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
