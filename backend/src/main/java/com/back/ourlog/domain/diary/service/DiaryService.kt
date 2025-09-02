package com.back.ourlog.domain.diary.service

import com.back.ourlog.domain.content.service.ContentService
import com.back.ourlog.domain.diary.dto.DiaryDetailDto
import com.back.ourlog.domain.diary.dto.DiaryResponseDto
import com.back.ourlog.domain.diary.dto.DiaryUpdateRequestDto
import com.back.ourlog.domain.diary.dto.DiaryWriteRequestDto
import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.diary.repository.DiaryRepository
import com.back.ourlog.domain.genre.service.GenreService
import com.back.ourlog.domain.ott.service.OttService
import com.back.ourlog.domain.tag.service.TagService
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.external.common.ContentSearchFacade
import com.back.ourlog.global.aop.Traceable
import com.back.ourlog.global.config.cache.CacheNames
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import com.back.ourlog.global.rq.Rq
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DiaryService(
    private val diaryRepository: DiaryRepository,
    private val contentService: ContentService,
    private val genreService: GenreService,
    private val tagService: TagService,
    private val ottService: OttService,
    private val contentSearchFacade: ContentSearchFacade,
    private val rq: Rq,

    @org.springframework.beans.factory.annotation.Value("\${ourlog.privacy.hide-private-diary:false}")
    private val hidePrivateDiary: Boolean
) {

    @Transactional
    fun writeWithContentSearch(req: DiaryWriteRequestDto, user: User): DiaryResponseDto {
        val result = contentSearchFacade.search(req.type, req.externalId)
            ?: throw CustomException(ErrorCode.CONTENT_NOT_FOUND)

        val content = contentService.saveOrGet(result, req.type)

        val diary = Diary(
            user = user,
            content = content,
            title = req.title,
            contentText = req.contentText,
            rating = req.rating,
            isPublic = req.isPublic
        )

        // 연관관계 동기화
        tagService.syncDiaryTags(diary, req.tagNames)
        genreService.syncDiaryGenres(diary, result.genres, content.type)
        ottService.syncDiaryOtts(diary, req.ottIds, content.type)

        val saved = diaryRepository.save(diary)
        return DiaryResponseDto.from(saved)
    }

    @Traceable
    @Transactional
    @CacheEvict(cacheNames = [CacheNames.DIARY_DETAIL], key = "#diaryId")
    fun update(diaryId: Int, dto: DiaryUpdateRequestDto, user: User): DiaryResponseDto {
        val diary = diaryRepository.findById(diaryId).orElseThrow { CustomException(ErrorCode.DIARY_NOT_FOUND) }

        // 작성자 검증
        if (diary.user.id != user.id) throw CustomException(ErrorCode.AUTH_FORBIDDEN)

        // 콘텐츠 변경 여부 확인
        val contentChanged =
            diary.content.externalId != dto.externalId || diary.content.type != dto.type

        if (contentChanged) {
            val result = contentSearchFacade.search(dto.type, dto.externalId)
                ?: throw CustomException(ErrorCode.CONTENT_NOT_FOUND)

            val newContent = contentService.saveOrGet(result, dto.type)
            diary.content = newContent

            // 콘텐츠 변경 시 장르 재계산
            genreService.syncDiaryGenres(diary, result.genres, newContent.type)
        }

        // 기본 필드 수정
        diary.update(dto.title, dto.contentText, dto.rating, dto.isPublic)

        // 연관관계 동기화
        tagService.syncDiaryTags(diary, dto.tagNames)
        ottService.syncDiaryOtts(diary, dto.ottIds, diary.content.type)

        return DiaryResponseDto.from(diary)
    }

    @Traceable
    @Cacheable(
        cacheNames = [CacheNames.DIARY_DETAIL],
        key = "#diaryId",
        unless = "#result == null || #result.isPublic == false"
    )
    fun getDiaryDetail(diaryId: Int): DiaryDetailDto {
        val diary = diaryRepository.findWithAllById(diaryId)
            .orElseThrow { CustomException(ErrorCode.DIARY_NOT_FOUND) }

        val currentUser = runCatching { rq.currentUser }.getOrNull()
        val isOwner = currentUser?.id == diary.user.id
        if (!diary.isPublic && !isOwner) {
            if (hidePrivateDiary) {
                // 존재 자체를 숨김
                throw CustomException(ErrorCode.DIARY_NOT_FOUND)
            } else {
                throw CustomException(ErrorCode.AUTH_FORBIDDEN)
            }
        }
        return DiaryDetailDto.from(diary)
    }

    @Transactional
    @CacheEvict(value = [CacheNames.DIARY_DETAIL], key = "#diaryId")
    fun delete(diaryId: Int, user: User) {
        val diary = diaryRepository.findById(diaryId)
            .orElseThrow { CustomException(ErrorCode.DIARY_NOT_FOUND) }

        if (diary.user.id != user.id) throw CustomException(ErrorCode.AUTH_FORBIDDEN)

        diaryRepository.delete(diary)
    }

    fun getDiariesByUser(
        userId: Int,
        pageable: Pageable,
        requester: User?
    ): Page<DiaryResponseDto> {
        val isOwner = requester?.id == userId

        val page = if (isOwner) {
            diaryRepository.findPageByUserIdWithToOne(userId, pageable)
        } else {
            diaryRepository.findPagePublicByUserIdWithToOne(userId, pageable)
        }

        return page.map { DiaryResponseDto.from(it) }
    }
}
