package io.swkoreatech.kosp.global.config.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swkoreatech.kosp.global.auth.token.TokenType;

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

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityRequirement securityRequirement = new SecurityRequirement();
        Components components = new Components();

        // ✅ 핵심: TokenType Enum을 돌면서 모든 헤더 키를 자동으로 등록
        for (TokenType type : TokenType.values()) {
            // 1. 헤더 이름 생성 (예: X-Access-Token)
            String headerKey = "X-" + StringUtils.capitalize(type.toString()) + "-Token";
            // 2. 스키마 식별자 (중복 방지용)
            String schemeName = type.name();

            // 3. 요구사항에 추가 (자물쇠 버튼 누르면 다 같이 입력하도록)
            securityRequirement.addList(schemeName);

            // 4. 스키마 등록 (API Key 방식)
            components.addSecuritySchemes(schemeName, new SecurityScheme()
                .name(headerKey) // 실제 HTTP 헤더 키
                .type(SecurityScheme.Type.APIKEY) // Bearer 아님
                .in(SecurityScheme.In.HEADER)
                .description(type.name() + " 토큰을 입력하세요.")
            );
        }

        return new OpenAPI()
            .addServersItem(new Server().url("/").description("Current server"))
            .addSecurityItem(securityRequirement)
            .components(components);
    }

    @Bean
    public org.springdoc.core.models.GroupedOpenApi defaultApi() {
        return org.springdoc.core.models.GroupedOpenApi.builder()
            .group("default")
            .pathsToMatch("/**")
            .pathsToExclude("/v1/admin/**")
            .build();
    }

    @Bean
    public org.springdoc.core.models.GroupedOpenApi adminApi() {
        return org.springdoc.core.models.GroupedOpenApi.builder()
            .group("admin")
            .pathsToMatch("/v1/admin/**")
            .build();
    }
}
