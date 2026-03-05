package io.swkoreatech.kosp.global.common.fixture;

import java.util.HashSet;

import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.common.user.model.User;

public final class TestUserFixture {

    private TestUserFixture() {
    }

    public static User createUser(Long id, String name) {
        User user = User.builder()
            .name(name)
            .kutId("2024" + id)
            .kutEmail(name + "@koreatech.ac.kr")
            .password("password")
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "roles", new HashSet<>());
        return user;
    }

    public static User createUser(Long id) {
        return createUser(id, "테스터");
    }

    public static User createUserWithPoint(Long id, Integer point) {
        User user = User.builder()
            .name("테스트유저")
            .kutId("2024" + id)
            .kutEmail("test" + id + "@koreatech.ac.kr")
            .password("password")
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "roles", new HashSet<>());
        ReflectionTestUtils.setField(user, "point", point);
        return user;
    }

    public static User createUserWithEmail(
        Long id, String email, boolean isDeleted
    ) {
        User user = User.builder()
            .name("테스터")
            .kutId("2024" + id)
            .kutEmail(email)
            .password("password")
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "roles", new HashSet<>());
        ReflectionTestUtils.setField(user, "isDeleted", isDeleted);
        return user;
    }
}
