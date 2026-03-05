package io.swkoreatech.kosp.domain.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;

import io.swkoreatech.kosp.global.common.IntegrationTestSupport;

@Sql("/data/repository-search-test.sql")
@DisplayName("RepositorySearch 통합 테스트")
public class RepositorySearchIntegrationTest extends IntegrationTestSupport {

    @Nested
    @DisplayName("기본 검색")
    class BasicSearchTest {

        @Test
        @DisplayName("keyword로 레포지토리를 검색한다")
        void searchByKeyword() throws Exception {
            // given & when
            MvcResult result = mockMvc.perform(get("/v1/search")
                    .param("keyword", "spring")
                    .param("filter", "repositories"))
                .andReturn();

            // then
            int status = result.getResponse().getStatus();
            String responseBody = result.getResponse().getContentAsString();

            assertThat(status).isEqualTo(200);
            assertThat(responseBody).isNotEmpty();

            JsonNode response = objectMapper.readTree(responseBody);
            assertThat(response.has("repositories")).isTrue();
            JsonNode repositories = response.get("repositories");
            assertThat(repositories.isArray()).isTrue();
            assertThat(repositories.size()).isGreaterThan(0);

            JsonNode firstRepo = repositories.get(0);
            assertThat(firstRepo.has("repoName")).isTrue();
            assertThat(firstRepo.has("description")).isTrue();
        }
    }

    @Nested
    @DisplayName("RSQL 필터")
    class RsqlFilterTest {

        @Test
        @DisplayName("stargazersCount>100 필터로 검색한다")
        void searchByStars() throws Exception {
            // given & when
            MvcResult result = mockMvc.perform(get("/v1/search")
                    .param("keyword", "")
                    .param("filter", "repositories")
                    .param("rsql", "stargazersCount>100"))
                .andExpect(status().isOk())
                .andReturn();

            // then
            String responseBody = result.getResponse().getContentAsString();
            JsonNode response = objectMapper.readTree(responseBody);
            JsonNode repositories = response.get("repositories");

            List<JsonNode> repoList = new ArrayList<>();
            repositories.forEach(repoList::add);
            assertThat(repoList).allSatisfy(repo ->
                assertThat(repo.get("stargazersCount").asInt()).isGreaterThan(100)
            );
        }

        @Test
        @DisplayName("primaryLanguage==Java 필터로 검색한다")
        void searchByLanguage() throws Exception {
            // given & when
            MvcResult result = mockMvc.perform(get("/v1/search")
                    .param("keyword", "")
                    .param("filter", "repositories")
                    .param("rsql", "primaryLanguage==Java"))
                .andExpect(status().isOk())
                .andReturn();

            // then
            String responseBody = result.getResponse().getContentAsString();
            JsonNode response = objectMapper.readTree(responseBody);
            JsonNode repositories = response.get("repositories");

            List<JsonNode> repoList = new ArrayList<>();
            repositories.forEach(repoList::add);
            assertThat(repoList).allSatisfy(repo ->
                assertThat(repo.get("primaryLanguage").asText()).isEqualTo("Java")
            );
        }
    }

    @Nested
    @DisplayName("복합 검색")
    class CombinedSearchTest {

        @Test
        @DisplayName("keyword + RSQL로 검색한다")
        void searchCombined() throws Exception {
            // given & when
            MvcResult result = mockMvc.perform(get("/v1/search")
                    .param("keyword", "KOSP")
                    .param("filter", "repositories")
                    .param("rsql", "stargazersCount>10"))
                .andExpect(status().isOk())
                .andReturn();

            // then
            String responseBody = result.getResponse().getContentAsString();
            JsonNode response = objectMapper.readTree(responseBody);
            JsonNode repositories = response.get("repositories");

            List<JsonNode> repoList = new ArrayList<>();
            repositories.forEach(repoList::add);
            assertThat(repoList).allSatisfy(repo -> {
                String repoName = repo.get("repoName").asText();
                String description = repo.get("description").asText();
                assertThat(repoName.contains("KOSP") || description.contains("KOSP"))
                    .isTrue();
                assertThat(repo.get("stargazersCount").asInt()).isGreaterThan(10);
            });
        }
    }

    @Nested
    @DisplayName("빈 결과 및 페이지네이션")
    class EmptyAndPaginationTest {

        @Test
        @DisplayName("매칭 없는 keyword 검색 시 빈 결과를 반환한다")
        void searchNoResults() throws Exception {
            // given & when
            MvcResult result = mockMvc.perform(get("/v1/search")
                    .param("keyword", "nonexistent12345xyz")
                    .param("filter", "repositories"))
                .andExpect(status().isOk())
                .andReturn();

            // then
            String responseBody = result.getResponse().getContentAsString();
            JsonNode response = objectMapper.readTree(responseBody);
            JsonNode repositories = response.get("repositories");
            assertThat(repositories.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("page=0&size=2로 페이지네이션 검색한다")
        void searchPagination() throws Exception {
            // given & when
            MvcResult result = mockMvc.perform(get("/v1/search")
                    .param("keyword", "")
                    .param("filter", "repositories")
                    .param("page", "0")
                    .param("size", "2"))
                .andExpect(status().isOk())
                .andReturn();

            // then
            String responseBody = result.getResponse().getContentAsString();
            JsonNode response = objectMapper.readTree(responseBody);
            JsonNode repositories = response.get("repositories");
            assertThat(repositories.size()).isLessThanOrEqualTo(2);

            JsonNode meta = response.get("meta");
            assertThat(meta.get("currentPage").asInt()).isEqualTo(0);
        }
    }
}
