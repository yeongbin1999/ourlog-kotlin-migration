package com.back.ourlog.global.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class TraceAspect {
    private val log = LoggerFactory.getLogger(TraceAspect::class.java)

    @Around("@within(com.back.ourlog.global.aop.Traceable) || @annotation(com.back.ourlog.global.aop.Traceable)")
    fun around(pjp: ProceedingJoinPoint): Any? {
        val method = "${pjp.signature.declaringType.simpleName}.${pjp.signature.name}"
        val start = System.currentTimeMillis()
        try {
            val result = pjp.proceed()
            val took = System.currentTimeMillis() - start
            log.info("TRACE ok method={} took={}ms", method, took)
            return result
        } catch (e: Throwable) {
            val took = System.currentTimeMillis() - start
            log.warn(
                "TRACE ex method={} took={}ms exClass={} exMsg={}",
                method, took, e::class.simpleName, e.message
            )
            throw e
        }
    }
}
