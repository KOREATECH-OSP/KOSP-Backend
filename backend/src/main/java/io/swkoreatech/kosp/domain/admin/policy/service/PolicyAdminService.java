package io.swkoreatech.kosp.domain.admin.policy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.auth.model.Permission;
import io.swkoreatech.kosp.common.auth.model.Policy;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.domain.admin.role.dto.request.PolicyCreateRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.request.PolicyUpdateRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.response.PermissionResponse;
import io.swkoreatech.kosp.domain.admin.role.dto.response.PolicyDetailResponse;
import io.swkoreatech.kosp.domain.admin.role.dto.response.PolicyResponse;
import io.swkoreatech.kosp.domain.auth.repository.PermissionRepository;
import io.swkoreatech.kosp.domain.auth.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;

/**
 * 정책 관리 서비스 (관리자).
 * <p>정책 CRUD, 권한 할당/제거 및 권한 조회 비즈니스 로직을 처리한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyAdminService {

    private final PolicyRepository policyRepository;
    private final PermissionRepository permissionRepository;

    /**
     * 모든 정책 목록을 조회한다.
     *
     * @return 정책 응답 DTO 목록
     */
    public List<PolicyResponse> getAllPolicies() {
        return policyRepository.findAll()
            .stream()
            .map(PolicyResponse::from)
            .toList();
    }

    /**
     * 특정 정책의 상세 정보를 조회한다.
     *
     * @param name 정책 이름
     * @return 정책 상세 응답 DTO
     * @throws GlobalException 정책을 찾을 수 없는 경우
     */
    public PolicyDetailResponse getPolicy(String name) {
        return PolicyDetailResponse.from(policyRepository.getByName(name));
    }

    /**
     * 새로운 정책을 생성한다.
     *
     * @param request 정책 생성 요청 DTO
     * @throws GlobalException 동일 이름의 정책이 이미 존재하는 경우
     */
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

    /**
     * 정책 설명을 수정한다.
     *
     * @param name    정책 이름
     * @param request 정책 수정 요청 DTO
     * @throws GlobalException 정책을 찾을 수 없는 경우
     */
    @Transactional
    public void updatePolicy(String name, PolicyUpdateRequest request) {
        Policy policy = policyRepository.getByName(name);
        policy.updateDescription(request.description());
    }

    /**
     * 정책을 삭제한다.
     *
     * @param name 삭제할 정책 이름
     * @throws GlobalException 정책이 역할에 의해 사용 중인 경우
     */
    @Transactional
    public void deletePolicy(String name) {
        Policy policy = policyRepository.getByName(name);
        if (!policy.getRoles().isEmpty()) {
            throw new GlobalException(ExceptionMessage.CONFLICT); // "Policy is in use by roles"
        }
        policyRepository.deleteByName(name);
    }

    /**
     * 정책에 권한을 할당한다.
     *
     * @param policyName     정책 이름
     * @param permissionName 할당할 권한 이름
     * @throws GlobalException 정책 또는 권한을 찾을 수 없는 경우
     */
    @Transactional
    public void assignPermission(String policyName, String permissionName) {
        Policy policy = policyRepository.getByName(policyName);
        Permission permission = permissionRepository.getByName(permissionName);

        policy.getPermissions().add(permission);
        policyRepository.save(policy);
    }

    /**
     * 정책에서 권한을 제거한다.
     *
     * @param policyName     정책 이름
     * @param permissionName 제거할 권한 이름
     * @throws GlobalException 정책 또는 권한을 찾을 수 없는 경우
     */
    @Transactional
    public void removePermission(String policyName, String permissionName) {
        Policy policy = policyRepository.getByName(policyName);
        Permission permission = permissionRepository.getByName(permissionName);

        policy.getPermissions().remove(permission);
        policyRepository.save(policy);
    }

    /**
     * 모든 권한 목록을 조회한다.
     *
     * @return 권한 응답 DTO 목록
     */
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll()
            .stream()
            .map(PermissionResponse::from)
            .toList();
    }

    /**
     * 특정 권한의 상세 정보를 조회한다.
     *
     * @param name 권한 이름
     * @return 권한 응답 DTO
     * @throws GlobalException 권한을 찾을 수 없는 경우
     */
    public PermissionResponse getPermission(String name) {
        return PermissionResponse.from(permissionRepository.getByName(name));
    }
}
