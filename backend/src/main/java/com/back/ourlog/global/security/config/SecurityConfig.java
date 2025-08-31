package com.back.ourlog.global.security.config;

import com.back.ourlog.global.security.exception.CustomAccessDeniedHandler;
import com.back.ourlog.global.security.exception.CustomAuthenticationEntryPoint;
import com.back.ourlog.global.security.filter.JwtAuthenticationFilter;
import com.back.ourlog.global.security.jwt.JwtAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtAuthenticationProvider);
    }



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF & CORS
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Stateless 세션 정책
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 예외 핸들링 (401, 403)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                // 4. URL 기반 접근 제어
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 허용
                        .requestMatchers(
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login",
                                "/api/v1/auth/reissue",
                                "/api/v1/auth/oauth/callback/**",
                                "/h2-console/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/auth/logout").authenticated()

                        // 컨텐츠
                        .requestMatchers("api/v1/contents/**").permitAll()

                        // 다이어리
                        .requestMatchers(HttpMethod.GET, "/api/v1/diaries/**").permitAll()
                        .requestMatchers("/api/v1/diaries/**").hasRole("USER")

                        // 댓글
                        .requestMatchers(HttpMethod.GET, "/api/v1/comments/*").permitAll()
                        .requestMatchers("/api/v1/comments/**").authenticated()

                        // Timeline 공개 API는 누구나 접근 가능..
                        .requestMatchers(HttpMethod.GET, "/api/v1/timeline").permitAll()

                        // follow 모두 로그인 필요..
                        .requestMatchers("/api/v1/follows/**").authenticated()

                        // 좋아요 등록/삭제는 로그인 필수..
                        .requestMatchers(HttpMethod.POST, "/api/v1/likes/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/likes/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/likes/count").permitAll()

                        // 그 외 모든 요청
                        .anyRequest().authenticated()
                )

                // 5. JWT 필터 등록
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                // 6. CSP 보안 헤더 설정 (옵션)
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("frame-ancestors 'self'"))
                );



        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "https://ourlog.vercel.app",
                "https://*.ourlog.shop"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization", "Set-Cookie")); // JWT 토큰 헤더

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}