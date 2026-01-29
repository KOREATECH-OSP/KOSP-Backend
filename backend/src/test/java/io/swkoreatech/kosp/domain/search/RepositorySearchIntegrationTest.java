package io.swkoreatech.kosp.domain.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;

import io.swkoreatech.kosp.global.common.IntegrationTestSupport;

@Sql("/data/repository-search-test.sql")
@DisplayName("Repository Search Integration Test")
public class RepositorySearchIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("기본 검색: keyword로 레포지토리 검색")
    void searchByKeyword() throws Exception {
        MvcResult resultNoFilter = mockMvc.perform(get("/v1/search")
                .param("keyword", "spring"))
            .andReturn();
        
        System.out.println("No filter status: " + resultNoFilter.getResponse().getStatus());
        System.out.println("No filter body length: " + resultNoFilter.getResponse().getContentAsString().length());
        
        MvcResult result = mockMvc.perform(get("/v1/search")
                .param("keyword", "spring")
                .param("filter", "repositories"))
            .andReturn();
        
        int status = result.getResponse().getStatus();
        String responseBody = result.getResponse().getContentAsString();
        
        assertThat(status).as("HTTP status should be 200").isEqualTo(200);
        assertThat(responseBody).as("Response body should not be empty (got " + responseBody.length() + " chars)").isNotEmpty();
        
        JsonNode response = objectMapper.readTree(responseBody);
        assertThat(response.has("repositories")).as("Response should have repositories field").isTrue();
        JsonNode repositories = response.get("repositories");
        assertThat(repositories.isArray()).as("repositories should be array").isTrue();
        assertThat(repositories.size()).as("should find spring repos").isGreaterThan(0);
        
        JsonNode firstRepo = repositories.get(0);
        assertThat(firstRepo.has("repoName")).isTrue();
        assertThat(firstRepo.has("description")).isTrue();
    }

    @Test
    @DisplayName("RSQL 필터: stargazersCount>100")
    void searchByStars() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/search")
                .param("keyword", "")
                .param("filter", "repositories")
                .param("rsql", "stargazersCount>100"))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode response = objectMapper.readTree(responseBody);
        
        JsonNode repositories = response.get("repositories");
        
        repositories.forEach(repo -> {
            int starCount = repo.get("stargazersCount").asInt();
            assertThat(starCount).isGreaterThan(100);
        });
    }

    @Test
    @DisplayName("RSQL 필터: primaryLanguage==Java")
    void searchByLanguage() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/search")
                .param("keyword", "")
                .param("filter", "repositories")
                .param("rsql", "primaryLanguage==Java"))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode response = objectMapper.readTree(responseBody);
        
        JsonNode repositories = response.get("repositories");
        
        repositories.forEach(repo -> {
            String language = repo.get("primaryLanguage").asText();
            assertThat(language).isEqualTo("Java");
        });
    }

    @Test
    @DisplayName("복합 검색: keyword + RSQL")
    void searchCombined() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/search")
                .param("keyword", "KOSP")
                .param("filter", "repositories")
                .param("rsql", "stargazersCount>10"))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode response = objectMapper.readTree(responseBody);
        
        JsonNode repositories = response.get("repositories");
        
        repositories.forEach(repo -> {
            String repoName = repo.get("repoName").asText();
            String description = repo.get("description").asText();
            int starCount = repo.get("stargazersCount").asInt();
            
            boolean containsKOSP = repoName.contains("KOSP") || description.contains("KOSP");
            assertThat(containsKOSP).isTrue();
            assertThat(starCount).isGreaterThan(10);
        });
    }

    @Test
    @DisplayName("빈 결과: 매칭 없는 keyword")
    void searchNoResults() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/search")
                .param("keyword", "nonexistent12345xyz")
                .param("filter", "repositories"))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode response = objectMapper.readTree(responseBody);
        
        JsonNode repositories = response.get("repositories");
        assertThat(repositories.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("페이지네이션: page=0&size=2")
    void searchPagination() throws Exception {
        MvcResult result = mockMvc.perform(get("/v1/search")
                .param("keyword", "")
                .param("filter", "repositories")
                .param("page", "0")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode response = objectMapper.readTree(responseBody);
        
        JsonNode repositories = response.get("repositories");
        assertThat(repositories.size()).isLessThanOrEqualTo(2);
        
        JsonNode meta = response.get("meta");
        assertThat(meta.get("size").asInt()).isEqualTo(2);
    }
}
