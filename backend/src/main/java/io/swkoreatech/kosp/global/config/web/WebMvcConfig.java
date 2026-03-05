package io.swkoreatech.kosp.global.config.web;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swkoreatech.kosp.global.auth.resolver.JwtArgumentResolver;
import io.swkoreatech.kosp.global.host.ClientURLArgumentResolver;
import io.swkoreatech.kosp.global.host.ClientURLInterceptor;
import io.swkoreatech.kosp.global.host.ServerURLArgumentResolver;
import io.swkoreatech.kosp.global.host.ServerURLInterceptor;
import lombok.RequiredArgsConstructor;

/**
 * Spring MVC 설정 클래스.
 * <p>커스텀 인터셉터(서버/클라이언트 URL)와 인자 리졸버(AuthUser, JWT, URL)를 등록하고,
 * 정적 리소스 핸들러를 설정한다.</p>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthUserArgumentResolver authUserArgumentResolver;
    private final ServerURLArgumentResolver serverURLArgumentResolver;
    private final ClientURLArgumentResolver clientURLArgumentResolver;
    private final ServerURLInterceptor serverURLInterceptor;
    private final ClientURLInterceptor clientURLInterceptor;
    private final JwtArgumentResolver jwtArgumentResolver;

    /** {@inheritDoc} */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authUserArgumentResolver);
        resolvers.add(serverURLArgumentResolver);
        resolvers.add(clientURLArgumentResolver);
        resolvers.add(jwtArgumentResolver);
    }

    /** {@inheritDoc} */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(serverURLInterceptor)
            .addPathPatterns("/**")
            .order(2);
        registry.addInterceptor(clientURLInterceptor)
            .addPathPatterns("/**")
            .order(3);
    }

    /** {@inheritDoc} */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-sort.js")
            .addResourceLocations("classpath:/static/");
    }
}
