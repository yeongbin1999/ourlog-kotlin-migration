package com.back.ourlog.global.security.filter;

import com.back.ourlog.global.security.jwt.JwtAuthenticationProvider;
import com.back.ourlog.global.security.jwt.JwtAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationProvider authenticationProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(token);

            // 예외 발생 시 자동으로 AuthenticationEntryPoint로 감
            Authentication authResult = authenticationProvider.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authResult);
        }

        filterChain.doFilter(request, response);
    }
}


