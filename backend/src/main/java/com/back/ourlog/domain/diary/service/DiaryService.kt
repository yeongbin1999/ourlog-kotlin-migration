package com.back.ourlog.domain.diary.service

import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.content.service.ContentService
import com.back.ourlog.domain.diary.dto.DiaryDetailDto
import com.back.ourlog.domain.diary.dto.DiaryResponseDto
import com.back.ourlog.domain.diary.dto.DiaryUpdateRequestDto
import com.back.ourlog.domain.diary.dto.DiaryWriteRequestDto
import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.diary.exception.DiaryNotFoundException
import com.back.ourlog.domain.diary.repository.DiaryRepository
import com.back.ourlog.domain.genre.entity.DiaryGenre
import com.back.ourlog.domain.genre.service.GenreService
import com.back.ourlog.domain.ott.entity.DiaryOtt
import com.back.ourlog.domain.ott.repository.OttRepository
import com.back.ourlog.domain.tag.entity.DiaryTag
import com.back.ourlog.domain.tag.entity.Tag
import com.back.ourlog.domain.tag.repository.TagRepository
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.external.common.ContentSearchFacade
import com.back.ourlog.external.library.service.LibraryService
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import com.back.ourlog.global.rq.Rq
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cache.annotation.CacheEvict
import org.springframework.core.env.Environment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DiaryService(
    private val diaryRepository: DiaryRepository,
    private val contentService: ContentService,
    private val genreService: GenreService,
    private val tagRepository: TagRepository,
    private val ottRepository: OttRepository,
    private val contentSearchFacade: ContentSearchFacade,
    private val libraryService: LibraryService,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
    private val env: Environment,
    private val rq: Rq
) {
    companion object {
        private const val CACHE_KEY_PREFIX = "diaryDetail::"
    }

    @Transactional
    fun writeWithContentSearch(req: DiaryWriteRequestDto, user: User): Diary {
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

        // 연관관계 설정
        updateTags(diary, req.tagNames)
        updateGenres(diary, result.genres, content.type)
        updateOtts(diary, req.ottIds, content.type)

        return diaryRepository.save(diary)
    }

    @Transactional
    fun update(diaryId: Int, dto: DiaryUpdateRequestDto, user: User): DiaryResponseDto {
        val diary = diaryRepository.findById(diaryId).orElseThrow { DiaryNotFoundException() }

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
            updateGenres(diary, result.genres, newContent.type)
        }

        // 기본 필드 수정
        diary.update(dto.title, dto.contentText, dto.rating, dto.isPublic)

        // 연관관계 동기화
        updateTags(diary, dto.tagNames)
        updateOtts(diary, dto.ottIds, diary.content.type)

        diaryRepository.flush()
        redisTemplate.delete("$CACHE_KEY_PREFIX$diaryId")

        return DiaryResponseDto.from(diary)
    }

    fun getDiaryDetail(diaryId: Int): DiaryDetailDto {
        val cacheKey = "$CACHE_KEY_PREFIX$diaryId"

        // 항상 엔티티 먼저 조회하여 비공개 권한 체크 선행
        val diary = diaryRepository.findById(diaryId).orElseThrow { DiaryNotFoundException() }

        val currentUser = runCatching { rq.currentUser }.getOrNull()
        if (!diary.isPublic && (currentUser == null || diary.user.id != currentUser.id)) {
            throw CustomException(ErrorCode.AUTH_FORBIDDEN)
        }

        val isTest = env.activeProfiles.any { it == "test" }
        if (!isTest) {
            val cached = redisTemplate.opsForValue().get(cacheKey)
            if (cached != null) {
                return objectMapper.convertValue(cached, DiaryDetailDto::class.java)
            }
        }

        val dto = DiaryDetailDto.from(diary)
        if (!isTest) {
            redisTemplate.opsForValue().set(cacheKey, dto)
        }
        return dto
    }

    @Transactional
    @CacheEvict(value = ["diaryDetail"], key = "#diaryId")
    fun delete(diaryId: Int, user: User) {
        val diary = diaryRepository.findById(diaryId)
            .orElseThrow { CustomException(ErrorCode.DIARY_NOT_FOUND) }

        if (diary.user.id != user.id) throw CustomException(ErrorCode.AUTH_FORBIDDEN)

        diaryRepository.delete(diary)
        redisTemplate.delete("$CACHE_KEY_PREFIX$diaryId")
    }

    @Transactional(readOnly = true)
    fun getDiariesByUser(userId: Int, pageable: Pageable, requester: User?): Page<DiaryResponseDto> {
        val isOwner = requester?.id == userId
        val diaries = if (isOwner) {
            diaryRepository.findByUserId(userId, pageable)
        } else {
            diaryRepository.findByUserIdAndIsPublicTrue(userId, pageable)
        }
        return diaries.map { DiaryResponseDto.from(it) }
    }

    /* -------------------- 연관관계 업데이트 -------------------- */

    // 이름 기반 upsert & 동기화
    private fun updateTags(diary: Diary, requestedNames: List<String>) {
        val currentNames = diary.diaryTags.map { it.tag.name }.toMutableSet()

        // 제거
        diary.diaryTags.removeIf { it.tag.name !in requestedNames }

        // 추가
        requestedNames
            .filterNot { it in currentNames }
            .forEach { name ->
                val tag: Tag = tagRepository.findByName(name) ?: tagRepository.save(Tag(name))
                diary.diaryTags.add(DiaryTag(diary, tag))
            }
    }

    // BOOK이면 KDC→장르 매핑, null이면 변경 없음
    private fun updateGenres(diary: Diary, rawNames: List<String>?, type: ContentType) {
        rawNames ?: return
        val mapped = rawNames.map { n -> if (type == ContentType.BOOK) libraryService.mapKdcToGenre(n) else n }
        val current = diary.diaryGenres.map { it.genre.name }.toMutableSet()

        diary.diaryGenres.removeIf { it.genre.name !in mapped }
        mapped
            .filterNot { it in current }
            .forEach { name -> diary.diaryGenres.add(DiaryGenre(diary, genreService.findOrCreateByName(name))) }
    }

    // MOVIE일 때만 유지, 그 외에는 모두 비움
    private fun updateOtts(diary: Diary, ottIds: List<Int>, type: ContentType) {
        if (type != ContentType.MOVIE) {
            diary.diaryOtts.clear()
            return
        }
        diary.diaryOtts.clear()
        ottIds.forEach { id ->
            val ott = ottRepository.findById(id).orElseThrow { CustomException(ErrorCode.OTT_NOT_FOUND) }
            diary.diaryOtts.add(DiaryOtt(diary, ott))
        }
    }
}
