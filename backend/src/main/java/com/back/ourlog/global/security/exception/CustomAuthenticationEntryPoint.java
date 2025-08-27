package com.back.ourlog.global.security.exception;

import com.back.ourlog.global.common.dto.RsData;
import com.back.ourlog.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        RsData<Void> body;

        if (authException instanceof JwtAuthenticationException jwtEx) {
            // 커스텀 예외에 담긴 세부 에러 코드로 응답
            body = RsData.fail(jwtEx.getErrorCode());
        } else {
            // 기타 인증 예외는 기본 Unauthorized 응답
            body = RsData.fail(ErrorCode.AUTH_UNAUTHORIZED);
        }

        String json = objectMapper.writeValueAsString(body);
        response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
    }

}
