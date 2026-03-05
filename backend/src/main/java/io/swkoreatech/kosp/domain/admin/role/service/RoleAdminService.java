package io.swkoreatech.kosp.domain.admin.role.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.auth.model.Policy;
import io.swkoreatech.kosp.common.auth.model.Role;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.admin.role.dto.request.RoleRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.request.RoleUpdateRequest;
import io.swkoreatech.kosp.domain.admin.role.dto.response.RoleResponse;
import io.swkoreatech.kosp.domain.auth.repository.PolicyRepository;
import io.swkoreatech.kosp.domain.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;

/**
 * 역할 관리 서비스 (관리자).
 * <p>역할 CRUD, 정책 할당/제거 비즈니스 로직을 처리한다.
 * SUPERUSER 역할은 수정 및 삭제가 불가하다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleAdminService {

    private static final String SUPERUSER_ROLE = "ROLE_SUPERUSER";

    private final RoleRepository roleRepository;
    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;

    /**
     * 모든 역할 목록을 조회한다.
     *
     * @return 역할 응답 DTO 목록
     */
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
            .map(RoleResponse::from)
            .toList();
    }

    /**
     * 특정 역할의 상세 정보를 조회한다.
     *
     * @param name 역할 이름
     * @return 역할 응답 DTO
     */
    public RoleResponse getRole(String name) {
        return RoleResponse.from(roleRepository.getByName(name));
    }

    /**
     * 새로운 역할을 생성한다.
     *
     * @param request 역할 생성 요청 DTO
     * @throws GlobalException 동일 이름의 역할이 이미 존재하는 경우
     */
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

    /**
     * 역할 정보를 수정한다.
     *
     * @param name    역할 이름
     * @param request 역할 수정 요청 DTO
     * @throws GlobalException SUPERUSER 역할인 경우
     */
    @Transactional
    public void updateRole(String name, RoleUpdateRequest request) {
        validateNotSuperuser(name);
        Role role = roleRepository.getByName(name);
        role.updateDescription(request.description());
        role.updateCanAccessAdmin(request.canAccessAdmin());
    }

    /**
     * 역할을 삭제한다.
     *
     * @param name 삭제할 역할 이름
     * @throws GlobalException SUPERUSER 역할이거나 사용 중인 역할인 경우
     */
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

    /**
     * 역할에 정책을 할당한다.
     *
     * @param roleName   역할 이름
     * @param policyName 할당할 정책 이름
     * @throws GlobalException SUPERUSER 역할인 경우
     */
    @Transactional
    public void assignPolicy(String roleName, String policyName) {
        validateNotSuperuser(roleName);

        Role role = roleRepository.getByName(roleName);
        Policy policy = policyRepository.getByName(policyName);

        role.getPolicies().add(policy);
    }

    /**
     * 역할에서 정책을 제거한다.
     *
     * @param roleName   역할 이름
     * @param policyName 제거할 정책 이름
     * @throws GlobalException SUPERUSER 역할인 경우
     */
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
