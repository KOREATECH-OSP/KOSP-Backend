package io.swkoreatech.kosp.global.common.fixture;

import java.util.HashSet;

import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.common.auth.model.Permission;
import io.swkoreatech.kosp.common.auth.model.Policy;
import io.swkoreatech.kosp.common.auth.model.Role;

public final class TestAuthFixture {

    private TestAuthFixture() {
    }

    public static Role createRole(Long id, String name) {
        Role role = Role.builder()
            .name(name)
            .description(name + " 역할")
            .policies(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(role, "id", id);
        return role;
    }

    public static Permission createPermission(Long id, String name) {
        Permission permission = Permission.builder()
            .name(name)
            .description(name + " 권한")
            .build();
        ReflectionTestUtils.setField(permission, "id", id);
        return permission;
    }

    public static Policy createPolicy(Long id, String name) {
        Policy policy = Policy.builder()
            .name(name)
            .description(name + " 정책")
            .permissions(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(policy, "id", id);
        return policy;
    }
}
