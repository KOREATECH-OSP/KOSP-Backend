package kr.ac.koreatech.sw.kosp.global.init;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Permission;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Policy;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.PermissionRepository;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.PolicyRepository;
import kr.ac.koreatech.sw.kosp.domain.auth.repository.RoleRepository;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

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
    public void run(String... args) throws Exception {
        log.info("Scanning for @Permit annotations...");
        Set<Permission> discoveredPermissions = scanPermissions();

        if (!discoveredPermissions.isEmpty()) {
            initAdminRole(discoveredPermissions);
        }
    }

    private Set<Permission> scanPermissions() {
        Set<Permission> permissions = new HashSet<>();
        Set<String> processedKeys = new HashSet<>();

        String[] beanNames = applicationContext.getBeanNamesForAnnotation(RestController.class);
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Method[] methods = bean.getClass().getMethods();
            
            for (Method method : methods) {
                Permit permit = method.getAnnotation(Permit.class);
                if (permit != null && !permit.permitAll() && !permit.name().isEmpty()) {
                    String key = permit.name();
                    if (!processedKeys.contains(key)) {
                        Permission permission = permissionRepository.findAll().stream()
                            .filter(p -> p.getName().equals(key))
                            .findFirst()
                            .orElseGet(() -> permissionRepository.save(
                                Permission.builder()
                                    .name(key)
                                    .description(permit.description().isEmpty() ? "Auto-generated" : permit.description())
                                    .build()
                            ));
                        permissions.add(permission);
                        processedKeys.add(key);
                    }
                }
            }
        }
        log.info("Discovered and Registered {} permissions.", permissions.size());
        return permissions;
    }

    private void initAdminRole(Set<Permission> permissions) {
        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            Policy adminPolicy = policyRepository.save(
                Policy.builder()
                    .name("AdminPolicy")
                    .description("Full Access Policy")
                    .permissions(permissions)
                    .build()
            );

            roleRepository.save(
                Role.builder()
                    .name("ROLE_ADMIN")
                    .description("System Administrator")
                    .policies(Set.of(adminPolicy))
                    .build()
            );
            log.info("Initialized ROLE_ADMIN with full permissions.");
        }
    }
}
