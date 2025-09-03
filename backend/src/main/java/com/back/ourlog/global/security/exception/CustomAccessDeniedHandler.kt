package com.back.ourlog.global.security.exception

import com.back.ourlog.global.common.dto.RsData.Companion.fail
import com.back.ourlog.global.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class CustomAccessDeniedHandler (
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest?, response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.contentType ="application/json;charset=UTF-8"
        response.status = HttpServletResponse.SC_FORBIDDEN

        val body = fail<Void?>(ErrorCode.AUTH_FORBIDDEN)

        val json = objectMapper.writeValueAsString(body)
        response.outputStream.write(json.toByteArray(StandardCharsets.UTF_8))
    }
}
