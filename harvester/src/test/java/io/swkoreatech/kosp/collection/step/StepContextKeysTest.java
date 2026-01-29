package io.swkoreatech.kosp.collection.step;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("StepContextKeys 단위 테스트")
class StepContextKeysTest {

    @Nested
    @DisplayName("상수 값 검증")
    class ConstantValueTest {

        @Test
        @DisplayName("모든 상수가 null이 아니다")
        void allConstants_areNotNull() {
            assertThat(StepContextKeys.GITHUB_LOGIN).isNotNull();
            assertThat(StepContextKeys.GITHUB_TOKEN).isNotNull();
            assertThat(StepContextKeys.GITHUB_NODE_ID).isNotNull();
            assertThat(StepContextKeys.DISCOVERED_REPOS).isNotNull();
        }

        @Test
        @DisplayName("모든 상수가 빈 문자열이 아니다")
        void allConstants_areNotEmpty() {
            assertThat(StepContextKeys.GITHUB_LOGIN).isNotEmpty();
            assertThat(StepContextKeys.GITHUB_TOKEN).isNotEmpty();
            assertThat(StepContextKeys.GITHUB_NODE_ID).isNotEmpty();
            assertThat(StepContextKeys.DISCOVERED_REPOS).isNotEmpty();
        }

        @Test
        @DisplayName("GITHUB_LOGIN 상수 값이 예상된 문자열과 일치한다")
        void githubLogin_hasExpectedValue() {
            assertThat(StepContextKeys.GITHUB_LOGIN).isEqualTo("githubLogin");
        }

        @Test
        @DisplayName("GITHUB_TOKEN 상수 값이 예상된 문자열과 일치한다")
        void githubToken_hasExpectedValue() {
            assertThat(StepContextKeys.GITHUB_TOKEN).isEqualTo("githubToken");
        }

        @Test
        @DisplayName("GITHUB_NODE_ID 상수 값이 예상된 문자열과 일치한다")
        void githubNodeId_hasExpectedValue() {
            assertThat(StepContextKeys.GITHUB_NODE_ID).isEqualTo("githubNodeId");
        }

        @Test
        @DisplayName("DISCOVERED_REPOS 상수 값이 예상된 문자열과 일치한다")
        void discoveredRepos_hasExpectedValue() {
            assertThat(StepContextKeys.DISCOVERED_REPOS).isEqualTo("discoveredRepos");
        }
    }

    @Nested
    @DisplayName("상수 유일성 검증")
    class UniquenessTest {

        @Test
        @DisplayName("모든 상수 값이 서로 다르다")
        void allConstants_areUnique() {
            Set<String> values = new HashSet<>();
            values.add(StepContextKeys.GITHUB_LOGIN);
            values.add(StepContextKeys.GITHUB_TOKEN);
            values.add(StepContextKeys.GITHUB_NODE_ID);
            values.add(StepContextKeys.DISCOVERED_REPOS);

            assertThat(values).hasSize(4);
        }

        @Test
        @DisplayName("중복된 값이 없다")
        void noDuplicateValues() {
            String[] constants = {
                StepContextKeys.GITHUB_LOGIN,
                StepContextKeys.GITHUB_TOKEN,
                StepContextKeys.GITHUB_NODE_ID,
                StepContextKeys.DISCOVERED_REPOS
            };

            for (int i = 0; i < constants.length; i++) {
                for (int j = i + 1; j < constants.length; j++) {
                    assertThat(constants[i]).isNotEqualTo(constants[j]);
                }
            }
        }
    }

    @Nested
    @DisplayName("상수 특성 검증")
    class CharacteristicTest {

        @Test
        @DisplayName("모든 상수가 camelCase 형식이다")
        void allConstants_useCamelCase() {
            assertThat(StepContextKeys.GITHUB_LOGIN).matches("^[a-z][a-zA-Z]*$");
            assertThat(StepContextKeys.GITHUB_TOKEN).matches("^[a-z][a-zA-Z]*$");
            assertThat(StepContextKeys.GITHUB_NODE_ID).matches("^[a-z][a-zA-Z]*$");
            assertThat(StepContextKeys.DISCOVERED_REPOS).matches("^[a-z][a-zA-Z]*$");
        }

        @Test
        @DisplayName("모든 상수가 영문자만 포함한다")
        void allConstants_containOnlyLetters() {
            assertThat(StepContextKeys.GITHUB_LOGIN).matches("^[a-zA-Z]+$");
            assertThat(StepContextKeys.GITHUB_TOKEN).matches("^[a-zA-Z]+$");
            assertThat(StepContextKeys.GITHUB_NODE_ID).matches("^[a-zA-Z]+$");
            assertThat(StepContextKeys.DISCOVERED_REPOS).matches("^[a-zA-Z]+$");
        }
    }
}
