package com.back.ourlog.global.security.config

import com.back.ourlog.global.security.exception.CustomAccessDeniedHandler
import com.back.ourlog.global.security.exception.CustomAuthenticationEntryPoint
import com.back.ourlog.global.security.filter.JwtAuthenticationFilter
import com.back.ourlog.global.security.jwt.JwtAuthenticationProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
class SecurityConfig(
    private val jwtAuthenticationProvider: JwtAuthenticationProvider,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun jwtAuthenticationFilter() = JwtAuthenticationFilter(jwtAuthenticationProvider)

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CSRF & CORS
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }

            // 세션 정책: Stateless
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            // 예외 처리
            .exceptionHandling {
                it.authenticationEntryPoint(customAuthenticationEntryPoint)
                it.accessDeniedHandler(customAccessDeniedHandler)
            }

            // URL 접근 제어
            .authorizeHttpRequests {
                // 인증 없이 접근 허용
                it.requestMatchers(
                    "/api/v1/auth/signup",
                    "/api/v1/auth/login",
                    "/api/v1/auth/reissue",
                    "/api/v1/auth/oauth/callback/**",
                    "/h2-console/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/health"
                ).permitAll()

                // 인증 필요
                it.requestMatchers("/api/v1/auth/logout").authenticated()
                it.requestMatchers("/api/v1/contents/**").permitAll()
                it.requestMatchers(HttpMethod.GET, "/api/v1/diaries/**").permitAll()
                it.requestMatchers("/api/v1/diaries/**").hasRole("USER")
                it.requestMatchers(HttpMethod.GET, "/api/v1/comments/**").permitAll()
                it.requestMatchers("/api/v1/comments/**").authenticated()
                it.requestMatchers(HttpMethod.GET, "/api/v1/timeline").permitAll()
                it.requestMatchers("/api/v1/follows/**").authenticated()
                it.requestMatchers(HttpMethod.POST, "/api/v1/likes/**").authenticated()
                it.requestMatchers(HttpMethod.DELETE, "/api/v1/likes/**").authenticated()
                it.requestMatchers(HttpMethod.GET, "/api/v1/likes/count").permitAll()

                // 나머지 요청
                it.anyRequest().authenticated()
            }

            // JWT 필터 등록
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)

            // FrameOptions (H2 console 등)
            .headers { it.frameOptions { it.sameOrigin() } }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOriginPatterns = listOf(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "https://ourlog.vercel.app",
                "https://*.ourlog.shop",
            )
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
            exposedHeaders = listOf("Authorization", "Set-Cookie")
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}