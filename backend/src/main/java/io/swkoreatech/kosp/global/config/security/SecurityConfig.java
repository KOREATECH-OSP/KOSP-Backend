package io.swkoreatech.kosp.global.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.global.auth.resolver.TokenHeaderResolver;
import io.swkoreatech.kosp.global.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

/**
 * Spring Security 설정 클래스.
 * <p>CSRF 비활성화, 무상태 세션 정책, CORS 설정, JWT 인증 필터 등록,
 * 비밀번호 인코더 및 인증 매니저를 구성한다.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final UserRepository userRepository;
    private final TokenHeaderResolver tokenHeaderResolver;

    /**
     * 보안 필터 체인을 구성한다.
     *
     * @param http HttpSecurity 설정 객체
     * @return 구성된 보안 필터 체인
     * @throws Exception 보안 설정 중 오류 발생 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .securityContext(context -> context.requireExplicitSave(false));

        http.addFilterBefore(
            new JwtAuthenticationFilter(userRepository, tokenHeaderResolver),
            UsernamePasswordAuthenticationFilter.class
        );

        http.exceptionHandling(exception -> exception
            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        );

        http.authorizeHttpRequests(auth ->
            auth.requestMatchers(
                    "/swagger",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/api-docs",
                    "/api-docs/**",
                    "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().permitAll()
        );

        return http.build();
    }

    /**
     * 위임 패턴 기반의 비밀번호 인코더 빈을 생성한다.
     *
     * @return 비밀번호 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 인증 매니저 빈을 생성한다.
     *
     * @param authenticationConfiguration 인증 구성 객체
     * @return 인증 매니저
     * @throws Exception 인증 매니저 생성 중 오류 발생 시
     */
    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
