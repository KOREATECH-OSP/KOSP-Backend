package io.swkoreatech.kosp.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

/**
 * V40__terms_type.sql 을 실제 PostgreSQL 에서 검증한다.
 *
 * <p>테스트 프로파일의 H2 는 Flyway 가 꺼져 있고 부분 유니크 인덱스({@code WHERE is_active})를
 * 지원하지 않으므로, 운영과 같은 마이그레이션 경로(V1 → V38 → 기존 데이터 존재 → V40)를
 * 임베디드 PostgreSQL 위에서 그대로 재현한다.</p>
 */
@DisplayName("V40 terms.type 마이그레이션")
class TermsTypeMigrationTest {

    private static final String INSERT_TERMS =
        "INSERT INTO terms (created_at, updated_at, type, version, content, is_active) "
            + "VALUES (NOW(), NOW(), ?, ?, 'content', ?)";

    private static EmbeddedPostgres postgres;
    private static JdbcTemplate jdbc;
    private static long baselineCount;

    @BeforeAll
    static void migrate() throws IOException {
        postgres = EmbeddedPostgres.start();
        DataSource dataSource = postgres.getPostgresDatabase();
        jdbc = new JdbcTemplate(dataSource);

        flyway(dataSource, "38").migrate();
        // V40 이전에 이미 쌓여 있던 과거 버전 약관 (V8 시드 '1.0' 은 활성 상태로 존재)
        jdbc.update("INSERT INTO terms (created_at, updated_at, version, content, is_active) "
            + "VALUES (NOW(), NOW(), '0.9', 'old', FALSE)");
        flyway(dataSource, "latest").migrate();

        baselineCount = count();
        printTerms("V40 적용 직후");
    }

    @AfterAll
    static void shutdown() throws IOException {
        postgres.close();
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM terms WHERE content = 'content'");
    }

    private static Flyway flyway(DataSource dataSource, String target) {
        return Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .target(target)
            .load();
    }

    private static long count() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM terms", Long.class);
    }

    private static void printTerms(String label) {
        System.out.println("=== [" + label + "] SELECT id, type, version, is_active FROM terms ORDER BY id;");
        jdbc.queryForList("SELECT id, type, version, is_active FROM terms ORDER BY id")
            .forEach(row -> System.out.println("    " + row));
    }

    private void insert(String type, String version, boolean active) {
        jdbc.update(INSERT_TERMS, type, version, active);
    }

    @Nested
    @DisplayName("기존 데이터 백필")
    class BackfillTest {

        @Test
        @DisplayName("V40 이전 레코드는 모두 SERVICE 로 채워지고, is_active 는 바뀌지 않는다")
        void existingRowsBecomeService() {
            List<Map<String, Object>> rows =
                jdbc.queryForList("SELECT type, version, is_active FROM terms ORDER BY version");

            assertThat(rows).extracting(row -> row.get("type")).containsOnly("SERVICE");
            assertThat(rows).extracting(row -> row.get("version"), row -> row.get("is_active"))
                .containsExactly(
                    tuple("0.9", false),
                    tuple("1.0", true));
        }

        @Test
        @DisplayName("type 컬럼은 NOT NULL 이다")
        void typeNotNull() {
            assertThatThrownBy(() -> jdbc.update(INSERT_TERMS, null, "9.9", false))
                .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("제약 이름")
    class ConstraintNameTest {

        @Test
        @DisplayName("uc_terms_version 은 제거되고 uc_terms_type_version, uq_terms_active_per_type 이 존재한다")
        void constraintsReplaced() {
            List<String> constraints = jdbc.queryForList(
                "SELECT conname FROM pg_constraint WHERE conrelid = 'terms'::regclass", String.class);
            List<String> indexes = jdbc.queryForList(
                "SELECT indexname FROM pg_indexes WHERE tablename = 'terms'", String.class);

            assertThat(constraints).contains("uc_terms_type_version").doesNotContain("uc_terms_version");
            assertThat(indexes).contains("uq_terms_active_per_type");
        }
    }

    @Nested
    @DisplayName("타입당 활성 약관 1개 제약 (uq_terms_active_per_type)")
    class ActivePerTypeTest {

        @Test
        @DisplayName("같은 type 에 활성 약관을 2개 넣으면 DB 제약으로 실패한다")
        void secondActiveSameType_fails() {
            assertThatThrownBy(() -> insert("SERVICE", "2.0", true))
                .isInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining("uq_terms_active_per_type");
            assertThat(count()).isEqualTo(baselineCount);
        }

        @Test
        @DisplayName("PRIVACY 활성 약관 2개도 실패한다")
        void twoActivePrivacy_fails() {
            insert("PRIVACY", "1.0", true);

            assertThatThrownBy(() -> insert("PRIVACY", "1.1", true))
                .isInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining("uq_terms_active_per_type");
        }

        @Test
        @DisplayName("type 이 다르면 활성 약관이 각각 1개씩 공존할 수 있다")
        void activePerDifferentType_ok() {
            assertThatCode(() -> insert("PRIVACY", "1.0", true)).doesNotThrowAnyException();
            Long active = jdbc.queryForObject("SELECT COUNT(*) FROM terms WHERE is_active", Long.class);
            assertThat(active).isEqualTo(2L);
        }

        @Test
        @DisplayName("비활성 약관은 같은 type 에 여러 개 있을 수 있다")
        void manyInactive_ok() {
            assertThatCode(() -> {
                insert("SERVICE", "0.8", false);
                insert("SERVICE", "0.7", false);
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("(type, version) 복합 유니크 (uc_terms_type_version)")
    class TypeVersionTest {

        @Test
        @DisplayName("같은 type·version 은 실패한다")
        void sameTypeVersion_fails() {
            assertThatThrownBy(() -> insert("SERVICE", "1.0", false))
                .isInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining("uc_terms_type_version");
        }

        @Test
        @DisplayName("type 이 다르면 같은 version 을 쓸 수 있다")
        void sameVersionDifferentType_ok() {
            assertThatCode(() -> insert("PRIVACY", "1.0", false)).doesNotThrowAnyException();
        }
    }
}
