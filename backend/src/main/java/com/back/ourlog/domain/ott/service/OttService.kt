package com.back.ourlog.domain.ott.service

import com.back.ourlog.domain.ott.entity.Ott
import com.back.ourlog.domain.ott.repository.OttRepository
import org.springframework.stereotype.Service

@Service
class OttService(
    private val ottRepository: OttRepository
) {

    fun getOttsByIds(ids: List<Int>): List<Ott> =
        ottRepository.findAllById(ids)
}
