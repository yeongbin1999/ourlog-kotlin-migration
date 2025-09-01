package com.back.ourlog.domain.content.service

import com.back.ourlog.domain.content.dto.ContentResponseDto
import com.back.ourlog.domain.content.dto.ContentSearchResultDto
import com.back.ourlog.domain.content.entity.Content
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.content.repository.ContentRepository
import com.back.ourlog.domain.diary.repository.DiaryRepository
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ContentService(
    private val diaryRepository: DiaryRepository,
    private val contentRepository: ContentRepository
) {

    fun getOrCreateContent(externalId: String, type: ContentType): Content {
        return contentRepository.findByExternalIdAndType(externalId, type)
            ?: contentRepository.save(
                Content(
                    title = "제목 없음",
                    type = type,
                    creatorName = "제작자",
                    description = null,
                    posterUrl = null,
                    releasedAt = LocalDateTime.now(),
                    externalId = externalId
                )
            )
    }

    fun getContent(diaryId: Int): ContentResponseDto =
        ContentResponseDto.from(
            diaryRepository.findById(diaryId)
                .orElseThrow { CustomException(ErrorCode.DIARY_NOT_FOUND) }
                .content
        )

    fun saveOrGet(result: ContentSearchResultDto, type: ContentType): Content {
        return contentRepository.findByExternalIdAndType(result.externalId, type)
            ?: contentRepository.save(Content.of(result))
    }
}
