package kr.ac.koreatech.sw.kosp.domain.admin.service;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request.PolicyCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.PermissionResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.role.dto.response.PolicyResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Permission;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.PermissionRepository;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.PolicyRepository;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void assignPermission(String policyName, String permissionName) {
        Policy policy = policyRepository.findByName(policyName)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
        
        Permission permission = permissionRepository.findByName(permissionName)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
        
        policy.getPermissions().add(permission);
        policyRepository.save(policy);
    }

    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll()
            .stream()
            .map(PermissionResponse::from)
            .toList();
    }
}
