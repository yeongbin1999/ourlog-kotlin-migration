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
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ContentService(
    private val diaryRepository: DiaryRepository,
    private val contentRepository: ContentRepository
) {

    fun getContent(diaryId: Int, userId: Int): ContentResponseDto {
        val diary = diaryRepository.findById(diaryId)
            .orElseThrow { CustomException(ErrorCode.DIARY_NOT_FOUND) }

        return ContentResponseDto.from(diary.content)
    }

    fun saveOrGet(result: ContentSearchResultDto, type: ContentType): Content {
        return contentRepository.findByExternalIdAndType(result.externalId, type)
            ?: contentRepository.save(Content.of(result))
    }
}