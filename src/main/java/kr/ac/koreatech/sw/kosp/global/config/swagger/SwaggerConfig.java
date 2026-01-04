package kr.ac.koreatech.sw.kosp.global.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "KOSP 백엔드 API 문서",
        description = "Koreatech Open Source Platform(KOSP) 백엔드 서비스의 REST API 명세서임.",
        version = "v1",
        contact = @Contact(
            name = "KOSP Backend Team",
            email = "contact@kosp.example.com"
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
        return new OpenAPI()
            .addServersItem(new Server().url("/").description("Current server (auto-detect HTTP/HTTPS)"));
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
