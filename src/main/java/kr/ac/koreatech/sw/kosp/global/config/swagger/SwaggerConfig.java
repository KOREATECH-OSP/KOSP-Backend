package kr.ac.koreatech.sw.kosp.global.config.swagger;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

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
        ),
        servers = {
                @Server(
                        description = "로컬 개발 서버",
                        url = "http://localhost:8080"
                )
        }
)
public class SwaggerConfig {
}


