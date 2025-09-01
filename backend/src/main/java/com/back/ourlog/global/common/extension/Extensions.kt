package com.back.ourlog.global.common.extension

import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull

fun <T : Any> T?.getOrThrow(errorCode: ErrorCode): T =
    this ?: throw CustomException(errorCode)

fun <T, ID: Any> CrudRepository<T, ID>.findByIdOrThrow(id: ID, errorCode: ErrorCode): T =
    this.findByIdOrNull(id) ?: throw CustomException(errorCode)