package kr.ac.koreatech.sw.kosp.global.config.web;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import kr.ac.koreatech.sw.kosp.global.host.ServerURLArgumentResolver;
import kr.ac.koreatech.sw.kosp.global.host.ServerURLInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthUserArgumentResolver authUserArgumentResolver;
    private final ServerURLArgumentResolver serverURLArgumentResolver;
    private final ServerURLInterceptor serverURLInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authUserArgumentResolver);
        resolvers.add(serverURLArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(serverURLInterceptor)
            .addPathPatterns("/**")
            .order(2);
    }
}
