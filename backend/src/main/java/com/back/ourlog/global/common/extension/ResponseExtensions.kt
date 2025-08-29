package com.back.ourlog.global.common.extension

import com.back.ourlog.global.common.dto.RsData
import com.back.ourlog.global.exception.ErrorCode
import org.springframework.http.ResponseEntity

// 성공 응답 확장
fun <T> T.toSuccessResponse(msg: String = "성공"): ResponseEntity<RsData<T>> =
    ResponseEntity.ok(RsData.success(msg, this))

// 실패 응답 확장
fun ErrorCode.toFailResponse(customMsg: String? = null): ResponseEntity<RsData<Nothing>> =
    ResponseEntity.status(this.status)
        .body(RsData.fail(this, customMsg))

// 성공 응답 (반환 데이터 없는 경우)
fun toSuccessResponseWithoutData(msg: String = "성공"): ResponseEntity<RsData<Void>> =
    ResponseEntity.ok(RsData.success(msg))