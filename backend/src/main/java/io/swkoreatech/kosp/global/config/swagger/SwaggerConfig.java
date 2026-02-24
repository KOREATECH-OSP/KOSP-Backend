package io.swkoreatech.kosp.global.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "KOSP 백엔드 API 문서",
        description = "Koreatech Open Source Platform(KOSP) 백엔드 서비스의 REST API 명세서",
        version = "v1",
        contact = @Contact(
            name = "KOSP Backend Team",
            email = "contact@swkoreatech.io"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    )
)
public class SwaggerConfig {

    private static final String ACCESS_TOKEN = "ACCESS";
    private static final String REFRESH_TOKEN = "REFRESH";
    private static final String SIGNUP_TOKEN = "SIGNUP";

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityRequirement securityRequirement = new SecurityRequirement()
            .addList(ACCESS_TOKEN)
            .addList(REFRESH_TOKEN)
            .addList(SIGNUP_TOKEN);

        Components components = new Components()
            .addSecuritySchemes(ACCESS_TOKEN, new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Access Token을 입력하세요. (Bearer 접두사 불필요)")
            )
            .addSecuritySchemes(REFRESH_TOKEN, new SecurityScheme()
                .name("X-Refresh-Token")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .description("Refresh Token을 입력하세요.")
            )
            .addSecuritySchemes(SIGNUP_TOKEN, new SecurityScheme()
                .name("X-Signup-Token")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .description("회원가입 토큰을 입력하세요.")
            );

        return new OpenAPI()
            .addServersItem(new Server().url("/").description("Current server"))
            .addSecurityItem(securityRequirement)
            .components(components)
            .tags(List.of(
                new Tag().name("Auth").description("인증 및 세션 관리 API"),
                new Tag().name("User").description("사용자 관리 API"),
                new Tag().name("User Activity").description("사용자 활동 조회 API"),
                new Tag().name("GitHub").description("GitHub 관련 API"),
                new Tag().name("Challenge").description("도전 과제 API"),
                new Tag().name("Notification").description("알림 API"),
                new Tag().name("Community - Board").description("게시판 메타데이터 API"),
                new Tag().name("Community - Article").description("게시글 관리 API"),
                new Tag().name("Community - Comment").description("댓글 관리 API"),
                new Tag().name("Community - Recruit").description("모집 공고 관리 API"),
                new Tag().name("Community - Report").description("신고 API"),
                new Tag().name("Team").description("팀 및 초대 관리 API"),
                new Tag().name("Search").description("통합 검색 API"),
                new Tag().name("Banner").description("배너 API"),
                new Tag().name("Upload").description("파일 업로드 API"),
                new Tag().name("Admin - Member").description("관리자 전용 사용자 관리 API"),
                new Tag().name("Admin - Role").description("관리자 전용 역할 관리 API"),
                new Tag().name("Admin - Permission").description("관리자 전용 권한 조회 API"),
                new Tag().name("Admin - Policy").description("관리자 전용 정책 관리 API"),
                new Tag().name("Admin - Point").description("관리자 전용 포인트 관리 API"),
                new Tag().name("Admin - Challenge").description("관리자 전용 챌린지 관리 API"),
                new Tag().name("Admin - Article").description("관리자 전용 게시글 관리 API"),
                new Tag().name("Admin - Content").description("관리자 전용 콘텐츠 관리 API"),
                new Tag().name("Admin - Report").description("관리자 전용 신고 관리 API"),
                new Tag().name("Admin - Contact").description("관리자 연락처 관리 API"),
                new Tag().name("Admin - Search").description("관리자 전용 통합 검색 API"),
                new Tag().name("Admin - Banner").description("배너 관리 API (관리자 전용)")
            ));
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
            .group("user")
            .displayName("01. 사용자")
            .pathsToMatch("/v1/auth/**", "/v1/users/**")
            .build();
    }

    @Bean
    public GroupedOpenApi communityApi() {
        return GroupedOpenApi.builder()
            .group("community")
            .displayName("02. 커뮤니티")
            .pathsToMatch("/v1/community/**")
            .build();
    }

    @Bean
    public GroupedOpenApi teamApi() {
        return GroupedOpenApi.builder()
            .group("team")
            .displayName("03. 팀")
            .pathsToMatch("/v1/teams/**")
            .build();
    }

    @Bean
    public GroupedOpenApi utilityApi() {
        return GroupedOpenApi.builder()
            .group("utility")
            .displayName("04. 부가기능")
            .pathsToMatch("/v1/search/**", "/v1/banner/**", "/v1/upload/**",
                "/v1/challenges/**", "/v1/notifications/**")
            .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
            .group("admin")
            .displayName("05. 관리자")
            .pathsToMatch("/v1/admin/**")
            .build();
    }
}
