package io.swkoreatech.kosp.global.config.swagger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

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
/**
 * Swagger/OpenAPI 문서 설정 클래스.
 * <p>API 그룹(사용자, 커뮤니티, 팀, 부가기능, 관리자)별로 분류하고,
 * JWT 토큰 기반 보안 스키마(ACCESS, REFRESH, SIGNUP)를 구성한다.</p>
 */
public class SwaggerConfig {

    private static final String ACCESS_TOKEN = "ACCESS";
    private static final String REFRESH_TOKEN = "REFRESH";
    private static final String SIGNUP_TOKEN = "SIGNUP";

    private static final List<String> USER_TAG_ORDER = List.of(
        "Auth",
        "User",
        "User Activity",
        "GitHub"
    );

    private static final List<String> COMMUNITY_TAG_ORDER = List.of(
        "Community - Board",
        "Community - Article",
        "Community - Comment",
        "Community - Recruit",
        "Community - Report"
    );

    private static final List<String> TEAM_TAG_ORDER = List.of(
        "Team"
    );

    private static final List<String> UTILITY_TAG_ORDER = List.of(
        "Challenge",
        "Notification",
        "Search",
        "Banner",
        "Upload"
    );

    private static final List<String> ADMIN_TAG_ORDER = List.of(
        "Admin - Member",
        "Admin - Role",
        "Admin - Permission",
        "Admin - Policy",
        "Admin - Point",
        "Admin - Challenge",
        "Admin - Article",
        "Admin - Content",
        "Admin - Report",
        "Admin - Contact",
        "Admin - Search",
        "Admin - Banner"
    );

    /**
     * 커스텀 OpenAPI 설정 빈을 생성한다.
     * <p>JWT 토큰 기반 보안 스키마와 서버 정보를 포함한다.</p>
     *
     * @return OpenAPI 설정 객체
     */
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
            .components(components);
    }

    /**
     * 사용자 관련 API 그룹을 생성한다.
     *
     * @return 사용자 API 그룹
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
            .group("user")
            .displayName("01. 사용자")
            .pathsToMatch("/v1/auth/**", "/v1/users/**")
            .addOpenApiCustomizer(retainUsedTagsOnly())
            .addOpenApiCustomizer(orderTags(USER_TAG_ORDER))
            .build();
    }

    /**
     * 커뮤니티 관련 API 그룹을 생성한다.
     *
     * @return 커뮤니티 API 그룹
     */
    @Bean
    public GroupedOpenApi communityApi() {
        return GroupedOpenApi.builder()
            .group("community")
            .displayName("02. 커뮤니티")
            .pathsToMatch("/v1/community/**")
            .addOpenApiCustomizer(retainUsedTagsOnly())
            .addOpenApiCustomizer(orderTags(COMMUNITY_TAG_ORDER))
            .build();
    }

    /**
     * 팀 관련 API 그룹을 생성한다.
     *
     * @return 팀 API 그룹
     */
    @Bean
    public GroupedOpenApi teamApi() {
        return GroupedOpenApi.builder()
            .group("team")
            .displayName("03. 팀")
            .pathsToMatch("/v1/teams/**")
            .addOpenApiCustomizer(retainUsedTagsOnly())
            .addOpenApiCustomizer(orderTags(TEAM_TAG_ORDER))
            .build();
    }

    /**
     * 부가기능 관련 API 그룹을 생성한다.
     *
     * @return 부가기능 API 그룹
     */
    @Bean
    public GroupedOpenApi utilityApi() {
        return GroupedOpenApi.builder()
            .group("utility")
            .displayName("04. 부가기능")
            .pathsToMatch("/v1/search/**", "/v1/banner/**", "/v1/upload/**",
                "/v1/challenges/**", "/v1/notifications/**")
            .addOpenApiCustomizer(retainUsedTagsOnly())
            .addOpenApiCustomizer(orderTags(UTILITY_TAG_ORDER))
            .build();
    }

    /**
     * 관리자 관련 API 그룹을 생성한다.
     *
     * @return 관리자 API 그룹
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
            .group("admin")
            .displayName("05. 관리자")
            .pathsToMatch("/v1/admin/**")
            .addOpenApiCustomizer(retainUsedTagsOnly())
            .addOpenApiCustomizer(orderTags(ADMIN_TAG_ORDER))
            .build();
    }

    /**
     * 모든 API 엔드포인트에 공통 에러 응답(400, 401, 403, 404, 409, 500)을 자동 등록한다.
     *
     * @return OperationCustomizer 빈
     */
    @Bean
    public OperationCustomizer globalErrorResponseCustomizer() {
        return (operation, handlerMethod) -> {
            ApiResponses responses = operation.getResponses();

            @SuppressWarnings("rawtypes")
            Schema<Object> errorSchema = new Schema<>()
                .type("object")
                .addProperty("message", new Schema<>().type("string").example("오류 메시지"))
                .addProperty("status", new Schema<>().type("integer").example(400));

            Map<String, String> errorResponses = Map.of(
                "400", "요청값 검증 실패",
                "401", "인증 실패",
                "403", "권한 부족",
                "404", "리소스 미존재",
                "409", "충돌/중복",
                "500", "서버 내부 오류"
            );

            errorResponses.forEach((code, description) -> {
                if (!responses.containsKey(code)) {
                    responses.addApiResponse(code, new ApiResponse()
                        .description(description)
                        .content(new Content().addMediaType("application/json",
                            new MediaType().schema(errorSchema))));
                }
            });

            return operation;
        };
    }

    private OpenApiCustomizer retainUsedTagsOnly() {
        return openApi -> {
            if (openApi.getPaths() == null || openApi.getTags() == null) {
                return;
            }
            Set<String> usedTags = openApi.getPaths().values().stream()
                .flatMap(pathItem -> pathItem.readOperations().stream())
                .filter(operation -> operation.getTags() != null)
                .flatMap(operation -> operation.getTags().stream())
                .collect(Collectors.toSet());
            openApi.getTags().removeIf(tag -> !usedTags.contains(tag.getName()));
        };
    }

    private OpenApiCustomizer orderTags(List<String> order) {
        return openApi -> {
            List<Tag> tags = openApi.getTags();
            if (CollectionUtils.isEmpty(tags)) {
                return;
            }
            Map<String, Tag> tagMap = tags.stream()
                .collect(Collectors.toMap(Tag::getName, tag -> tag, (a, b) -> a));
            List<Tag> ordered = new ArrayList<>();
            order.stream()
                .filter(tagMap::containsKey)
                .map(tagMap::get)
                .forEach(ordered::add);
            tags.stream()
                .filter(tag -> !order.contains(tag.getName()))
                .sorted(Comparator.comparing(Tag::getName))
                .forEach(ordered::add);
            openApi.setTags(ordered);
        };
    }
}
