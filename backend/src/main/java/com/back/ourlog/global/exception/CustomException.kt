package com.back.ourlog.global.exception

open class CustomException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message) {

}