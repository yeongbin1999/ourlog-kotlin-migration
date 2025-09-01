package com.back.ourlog.domain.ott.service

import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.ott.entity.DiaryOtt
import com.back.ourlog.domain.ott.repository.OttRepository
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OttService(
    private val ottRepository: OttRepository
) {
    // 다이어리-OTT 동기화
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    fun syncDiaryOtts(diary: Diary, ottIds: List<Int>, type: ContentType) {
        // MOVIE가 아니면 유지하지 않음: 변경이 있을 때만 clear
        if (type != ContentType.MOVIE) {
            if (diary.diaryOtts.isNotEmpty()) diary.diaryOtts.clear()
            return
        }

        val requested = ottIds.distinct()
        val current   = diary.diaryOtts.mapNotNull { it.ott.id }.toSet()

        val toAddIds    = requested.filter { it !in current }
        val toRemoveIds = current.filter   { it !in requested }

        if (toAddIds.isEmpty() && toRemoveIds.isEmpty()) return // 변경 없음

        // 제거
        val it = diary.diaryOtts.iterator()
        while (it.hasNext()) {
            val rel = it.next()
            val id  = rel.ott.id
            if (id != null && id in toRemoveIds) it.remove()
        }

        // 추가
        if (toAddIds.isNotEmpty()) {
            val otts = ottRepository.findAllById(toAddIds)
            val loadedIds = otts.mapNotNull { it.id }.toSet()
            val missing = toAddIds.filter { it !in loadedIds }
            if (missing.isNotEmpty()) throw CustomException(ErrorCode.OTT_NOT_FOUND)

            otts.forEach { ott -> diary.diaryOtts.add(DiaryOtt(diary, ott)) }
        }
    }
}
