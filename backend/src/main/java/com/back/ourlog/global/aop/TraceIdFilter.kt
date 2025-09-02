package com.back.ourlog.global.aop

import com.back.ourlog.global.rq.Rq
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.beans.factory.ObjectProvider
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * 요청마다 traceId/userId를 MDC에 심어 로그 라인에 자동으로 태깅.
 * - userId는 인증 이후에만 얻을 수 있으므로, 필터 순서를 Security 필터 뒤로 두는 게 안전.
 */

@Order(Ordered.LOWEST_PRECEDENCE - 10)
@Component
class TraceIdFilter(
    private val rqProvider: ObjectProvider<Rq>
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val traceId = request.getHeader("X-Trace-Id") ?: UUID.randomUUID().toString().take(8)

        // 지연 획득 + 예외 안전
        val userId = runCatching { rqProvider.ifAvailable?.currentUser?.id?.toString() }
            .getOrNull() ?: "anonymous"

        MDC.put("traceId", traceId)
        MDC.put("userId", userId)
        try {
            response.addHeader("X-Trace-Id", traceId)
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}
