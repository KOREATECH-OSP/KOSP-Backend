package io.swkoreatech.kosp.domain.admin.policy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.admin.role.dto.request.PolicyCreateRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.request.PolicyUpdateRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.response.PermissionResponse;
import io.swkoreatech.kosp.domain.admin.role.dto.response.PolicyDetailResponse;
import io.swkoreatech.kosp.domain.admin.role.dto.response.PolicyResponse;
import io.swkoreatech.kosp.domain.auth.model.Permission;
import io.swkoreatech.kosp.domain.auth.model.Policy;
import io.swkoreatech.kosp.domain.auth.repository.PermissionRepository;
import io.swkoreatech.kosp.domain.auth.repository.PolicyRepository;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyAdminService {

    private final PolicyRepository policyRepository;
    private final PermissionRepository permissionRepository;

    public List<PolicyResponse> getAllPolicies() {
        return policyRepository.findAll()
            .stream()
            .map(PolicyResponse::from)
            .toList();
    }

    public PolicyResponse getPolicy(String name) {
        return PolicyResponse.from(findPolicy(name));
    }

    @Transactional
    public void createPolicy(PolicyCreateRequest request) {
        if (policyRepository.findByName(request.name()).isPresent()) {
            throw new GlobalException(ExceptionMessage.CONFLICT);
        }

        Policy policy = Policy.builder()
            .name(request.name())
            .description(request.description())
            .build();

        policyRepository.save(policy);
    }

    @Transactional
    public void updatePolicy(String name, PolicyUpdateRequest request) {
        Policy policy = findPolicy(name);
        policy.updateDescription(request.description());
    }

    @Transactional
    public void deletePolicy(String name) {
        Policy policy = findPolicy(name);
        if (!policy.getRoles().isEmpty()) {
            throw new GlobalException(ExceptionMessage.CONFLICT); // "Policy is in use by roles"
        }
        policyRepository.deleteByName(name);
    }

    @Transactional
    public void assignPermission(String policyName, String permissionName) {
        Policy policy = findPolicy(policyName);
        Permission permission = findPermission(permissionName);
        
        policy.getPermissions().add(permission);
        policyRepository.save(policy);
    }

    @Transactional
    public void removePermission(String policyName, String permissionName) {
        Policy policy = findPolicy(policyName);
        Permission permission = findPermission(permissionName);
        
        policy.getPermissions().remove(permission);
        policyRepository.save(policy);
    }

    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll()
            .stream()
            .map(PermissionResponse::from)
            .toList();
    }

    public PermissionResponse getPermission(String name) {
        return PermissionResponse.from(findPermission(name));
    }

    private Policy findPolicy(String name) {
        return policyRepository.findByName(name)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }

    private Permission findPermission(String name) {
        return permissionRepository.findByName(name)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }
}
