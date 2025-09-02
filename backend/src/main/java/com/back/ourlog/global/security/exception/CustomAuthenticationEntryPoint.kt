package com.back.ourlog.global.security.exception

import com.back.ourlog.global.common.dto.RsData
import com.back.ourlog.global.common.dto.RsData.Companion.fail
import com.back.ourlog.global.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class CustomAuthenticationEntryPoint (
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest?, response: HttpServletResponse,
        authException: AuthenticationException?
    ) {
        response.setContentType("application/json;charset=UTF-8")
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)

        val body: RsData<Void?>?

        if (authException is JwtAuthenticationException) {
            // 커스텀 예외에 담긴 세부 에러 코드로 응답
            body = fail<Void?>(authException.errorCode)
        } else {
            // 기타 인증 예외는 기본 Unauthorized 응답
            body = fail<Void?>(ErrorCode.AUTH_UNAUTHORIZED)
        }

        val json = objectMapper.writeValueAsString(body)
        response.outputStream.write(json.toByteArray(StandardCharsets.UTF_8))
    }
}
