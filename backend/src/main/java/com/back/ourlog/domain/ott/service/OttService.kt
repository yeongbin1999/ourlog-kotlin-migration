package com.back.ourlog.domain.ott.service

import com.back.ourlog.domain.ott.entity.Ott
import com.back.ourlog.domain.ott.repository.OttRepository
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class OttService(
    private val ottRepository: OttRepository
) {

    fun getOttsByIds(ids: List<Int>): List<Ott> {
        val otts = ottRepository.findAllById(ids)
        if (otts.isEmpty()) {
            throw CustomException(ErrorCode.OTT_NOT_FOUND)
        }
        return otts
    }
}
