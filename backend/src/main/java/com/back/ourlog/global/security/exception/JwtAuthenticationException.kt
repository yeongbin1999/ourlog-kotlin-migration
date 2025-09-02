package com.back.ourlog.global.security.exception

import com.back.ourlog.global.exception.ErrorCode
import org.springframework.security.core.AuthenticationException

class JwtAuthenticationException : AuthenticationException {
    val errorCode: ErrorCode

    constructor(errorCode: ErrorCode) : super(errorCode.message) {
        this.errorCode = errorCode
    }

    constructor(errorCode: ErrorCode, cause: Throwable?) : super(errorCode.message, cause) {
        this.errorCode = errorCode
    }
}
