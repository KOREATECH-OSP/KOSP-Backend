package io.swkoreatech.kosp.global.config.web;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swkoreatech.kosp.global.auth.resolver.JwtArgumentResolver;
import io.swkoreatech.kosp.global.host.ClientURLArgumentResolver;
import io.swkoreatech.kosp.global.host.ClientURLInterceptor;
import io.swkoreatech.kosp.global.host.ServerURLArgumentResolver;
import io.swkoreatech.kosp.global.host.ServerURLInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthUserArgumentResolver authUserArgumentResolver;
    private final ServerURLArgumentResolver serverURLArgumentResolver;
    private final ClientURLArgumentResolver clientURLArgumentResolver;
    private final ServerURLInterceptor serverURLInterceptor;
    private final ClientURLInterceptor clientURLInterceptor;
    private final JwtArgumentResolver jwtArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authUserArgumentResolver);
        resolvers.add(serverURLArgumentResolver);
        resolvers.add(clientURLArgumentResolver);
        resolvers.add(jwtArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(serverURLInterceptor)
            .addPathPatterns("/**")
            .order(2);
        registry.addInterceptor(clientURLInterceptor)
            .addPathPatterns("/**")
            .order(3);
    }
}
