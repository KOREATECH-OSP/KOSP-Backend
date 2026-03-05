package io.swkoreatech.kosp.global.config.web;

import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.apache.tomcat.util.http.SameSiteCookies;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 쿠키 설정 구성 클래스.
 * <p>SameSite 속성 및 Secure 플래그를 설정하여 크로스 사이트 요청에서의 쿠키 동작을 제어한다.</p>
 */
@Configuration
public class CookieConfig {

    /**
     * Tomcat 쿠키 프로세서를 커스터마이징하여 SameSite=None을 설정한다.
     *
     * @return 웹 서버 팩토리 커스터마이저
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieProcessorCustomizer() {
        return factory -> factory.addContextCustomizers(context -> {
            Rfc6265CookieProcessor cookieProcessor = new Rfc6265CookieProcessor();
            cookieProcessor.setSameSiteCookies(SameSiteCookies.NONE.getValue());
            context.setCookieProcessor(cookieProcessor);
        });
    }

    /**
     * 세션 쿠키에 Secure 플래그를 설정하는 서블릿 컨텍스트 초기화 빈을 생성한다.
     *
     * @return 서블릿 컨텍스트 초기화 객체
     */
    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return servletContext -> servletContext.getSessionCookieConfig().setSecure(true);
    }
}
