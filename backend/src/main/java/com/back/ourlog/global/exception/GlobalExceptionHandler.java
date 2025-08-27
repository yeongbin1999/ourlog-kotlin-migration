package com.back.ourlog.global.exception;

import com.back.ourlog.global.common.dto.RsData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<RsData<Void>> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        String message = ex.getMessage() != null ? ex.getMessage() : errorCode.getMessage();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(RsData.fail(errorCode, message));
    }

    // IllegalArgumentException → BAD_REQUEST로 통일
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RsData<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(RsData.fail(ErrorCode.BAD_REQUEST, e.getMessage()));
    }

    // @Valid 실패 → BAD_REQUEST
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : ErrorCode.BAD_REQUEST.getMessage();

        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(RsData.fail(ErrorCode.BAD_REQUEST, errorMessage));
    }

    // 필수 요청 파라미터 누락 → BAD_REQUEST
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RsData<Void>> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        String paramName = e.getParameterName();
        String message = "필수 요청 파라미터 '" + paramName + "'가 누락되었습니다.";

        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(RsData.fail(ErrorCode.BAD_REQUEST, message));
    }

    // 예상 못한 예외 → SERVER_ERROR
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RsData<Void>> handleException(Exception e) {
        e.printStackTrace(); // 로그 남기기

        return ResponseEntity
                .status(ErrorCode.SERVER_ERROR.getStatus())
                .body(RsData.fail(ErrorCode.SERVER_ERROR, "서버 오류: " + e.getMessage()));
    }
}
