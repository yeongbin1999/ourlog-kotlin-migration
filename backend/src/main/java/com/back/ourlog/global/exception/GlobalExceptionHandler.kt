package com.back.ourlog.global.exception

import com.back.ourlog.global.common.dto.RsData
import com.back.ourlog.global.common.extension.toFailResponse
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.ResponseEntity
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    // CustomException 처리
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(ex: CustomException): ResponseEntity<RsData<Nothing>> {
        return ex.errorCode.toFailResponse()
    }

    // IllegalArgumentException → BAD_REQUEST로 통일
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<RsData<Nothing>> {
        return ErrorCode.BAD_REQUEST.toFailResponse(e.message)
    }

    // @Valid 실패 → BAD_REQUEST
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<RsData<Nothing>> {
        val errorMessage = e.bindingResult.fieldError?.defaultMessage
            ?: ErrorCode.BAD_REQUEST.message

        return ErrorCode.BAD_REQUEST.toFailResponse(errorMessage)
    }

    // 필수 요청 파라미터 누락 → BAD_REQUEST
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameter(e: MissingServletRequestParameterException): ResponseEntity<RsData<Nothing>> {
        val paramName = e.parameterName
        val message = "필수 요청 파라미터 '$paramName'가 누락되었습니다."

        return ErrorCode.BAD_REQUEST.toFailResponse(message)
    }

    // 낙관적 락 충돌 → 409
    @ExceptionHandler(
        ObjectOptimisticLockingFailureException::class,
        OptimisticLockingFailureException::class
    )
    fun handleOptimisticLock(e: Exception): ResponseEntity<RsData<Nothing>> {
        return ErrorCode.CONFLICT_VERSION.toFailResponse("다른 사용자가 먼저 수정했습니다. 새로고침 후 다시 시도해 주세요.")
    }

    // 예상 못한 예외 → SERVER_ERROR
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<RsData<Nothing>> {
        e.printStackTrace() // 로그 남기기

        return ErrorCode.SERVER_ERROR.toFailResponse("서버 오류: ${e.message}")
    }
}
