package com.back.ourlog.global.common.dto

import com.back.ourlog.global.exception.ErrorCode

data class RsData<T>(
    val resultCode: String,
    val msg: String? = null,
    val data: T? = null
) {

    val isSuccess: Boolean
        get() = resultCode.startsWith("SUCCESS")

    val isFail: Boolean
        get() = !isSuccess

    companion object {
        @JvmStatic
        @JvmOverloads
        fun <T> success(msg: String, data: T? = null) = RsData("SUCCESS_200", msg, data)

        @JvmStatic
        @JvmOverloads
        fun <T> fail(errorCode: ErrorCode, customMsg: String? = null): RsData<T> =
            RsData(errorCode.code, customMsg ?: errorCode.message, null)

        @JvmStatic
        fun <T> of(resultCode: String, msg: String, data: T? = null) = RsData(resultCode, msg, data)
    }
}