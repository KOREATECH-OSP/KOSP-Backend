package io.swkoreatech.kosp.global.init;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.aop.support.AopUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.auth.model.Permission;
import io.swkoreatech.kosp.domain.auth.model.Policy;
import io.swkoreatech.kosp.domain.auth.model.Role;
import io.swkoreatech.kosp.domain.auth.repository.PermissionRepository;
import io.swkoreatech.kosp.domain.auth.repository.PolicyRepository;
import io.swkoreatech.kosp.domain.auth.repository.RoleRepository;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInitializer implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final PermissionRepository permissionRepository;
    private final PolicyRepository policyRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Scanning for @Permit annotations...");
        Set<Permission> discovered = scanPermissions();

        if (discovered.isEmpty()) {
            return;
        }
        initRoles(discovered);
    }

    private Set<Permission> scanPermissions() {
        Map<String, Permission> existing = loadExistingPermissions();
        Set<Permission> results = new HashSet<>();

        for (String beanName : applicationContext.getBeanNamesForAnnotation(RestController.class)) {
            processBean(beanName, existing, results);
        }
        log.info("Discovered and Registered {} permissions.", results.size());
        return results;
    }

    private Map<String, Permission> loadExistingPermissions() {
        return permissionRepository.findAll().stream()
            .collect(Collectors.toMap(Permission::getName, Function.identity()));
    }

    private void processBean(String beanName, Map<String, Permission> existing, Set<Permission> results) {
        Object bean = applicationContext.getBean(beanName);
        for (Method method : AopUtils.getTargetClass(bean).getMethods()) {
            processMethod(method, existing, results);
        }
    }

    private void processMethod(Method method, Map<String, Permission> existing, Set<Permission> results) {
        Permit permit = method.getAnnotation(Permit.class);
        if (isValidPermit(permit)) {
            results.add(getOrCreatePermission(permit, existing));
        }
    }

    private boolean isValidPermit(Permit permit) {
        return permit != null && !permit.permitAll() && !permit.name().isEmpty();
    }

    private Permission getOrCreatePermission(Permit permit, Map<String, Permission> existing) {
        if (existing.containsKey(permit.name())) {
            return existing.get(permit.name());
        }
        return createPermission(permit, existing);
    }

    private Permission createPermission(Permit permit, Map<String, Permission> existing) {
        Permission permission = permissionRepository.save(
            Permission.builder()
                .name(permit.name())
                .description(resolveDescription(permit))
                .build()
        );
        existing.put(permit.name(), permission);
        return permission;
    }

    private String resolveDescription(Permit permit) {
        return permit.description().isEmpty() ? "Auto-generated" : permit.description();
    }

    private void initRoles(Set<Permission> permissions) {
        initSuperuserRole();
        initAdminRole(permissions);
        initStudentRole(permissions);
        initEmployeeRole(permissions);
    }
    
    private void initSuperuserRole() {
        if (roleRepository.existsByName("ROLE_SUPERUSER")) {
            return;
        }
        
        Role superuserRole = roleRepository.save(
            Role.builder()
                .name("ROLE_SUPERUSER")
                .description("슈퍼유저 (모든 권한 보유, 수정 불가)")
                .build()
        );
        
        log.info("Initialized ROLE_SUPERUSER (immutable)");
    }

    private void initAdminRole(Set<Permission> permissions) {
        createRoleIfNotExists("ROLE_ADMIN", "시스템 관리자", "AdminPolicy", "시스템 관리자 정책 (모든 권한)", permissions);
    }

    private void initStudentRole(Set<Permission> permissions) {
        Set<Permission> filtered = filterPermissions(permissions, "article", "recruit", "comment", "team");
        createRoleIfNotExists("ROLE_STUDENT", "학생", "StudentPolicy", "학생 권한 정책 (게시글 및 모집 공고 관리)", filtered);
    }

    private void initEmployeeRole(Set<Permission> permissions) {
        Set<Permission> filtered = filterPermissions(permissions, "article", "recruit", "comment", "team");
        createRoleIfNotExists("ROLE_EMPLOYEE", "교직원", "EmployeePolicy", "교직원 권한 정책 (게시글 및 모집 공고 관리)", filtered);
    }

    private void createRoleIfNotExists(
        String roleName, String roleDesc, String policyName, String policyDesc, Set<Permission> permissions
    ) {
        Policy policy = policyRepository.findByName(policyName)
            .orElseGet(() -> createPolicy(policyName, policyDesc, permissions));

        // If policy exists, we do NOT overwrite permissions.
        // This respects dynamic changes made by Admin.
        // policy.updatePermissions(permissions);
        // policyRepository.save(policy);

        if (roleRepository.existsByName(roleName)) {
            return;
        }
        createRole(roleName, roleDesc, policy);
        log.info("Initialized {}.", roleName);
    }

    private Policy createPolicy(String name, String description, Set<Permission> permissions) {
        return policyRepository.save(
            Policy.builder()
                .name(name)
                .description(description)
                .permissions(permissions)
                .build()
        );
    }

    private void createRole(String name, String description, Policy policy) {
        roleRepository.save(
            Role.builder()
                .name(name)
                .description(description)
                .policies(Set.of(policy))
                .build()
        );
    }

    private Set<Permission> filterPermissions(Set<Permission> permissions, String... prefixes) {
        return permissions.stream()
            .filter(p -> Arrays.stream(prefixes).anyMatch(prefix -> p.getName().startsWith(prefix + ":")))
            .collect(Collectors.toSet());
    }
}
