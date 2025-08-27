package com.back.ourlog.domain.diary.service;

import com.back.ourlog.domain.content.dto.ContentSearchResultDto;
import com.back.ourlog.domain.content.entity.Content;
import com.back.ourlog.domain.content.service.ContentService;
import com.back.ourlog.domain.diary.dto.DiaryDetailDto;
import com.back.ourlog.domain.diary.dto.DiaryResponseDto;
import com.back.ourlog.domain.diary.dto.DiaryUpdateRequestDto;
import com.back.ourlog.domain.diary.dto.DiaryWriteRequestDto;
import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.diary.exception.DiaryNotFoundException;
import com.back.ourlog.domain.diary.factory.DiaryFactory;
import com.back.ourlog.domain.diary.mapper.DiaryMapper;
import com.back.ourlog.domain.diary.repository.DiaryRepository;
import com.back.ourlog.domain.genre.service.GenreService;
import com.back.ourlog.domain.ott.repository.OttRepository;
import com.back.ourlog.domain.tag.repository.TagRepository;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.external.common.ContentSearchFacade;
import com.back.ourlog.external.library.service.LibraryService;
import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import com.back.ourlog.global.rq.Rq;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final ContentService contentService;
    private final GenreService genreService;
    private final TagRepository tagRepository;
    private final OttRepository ottRepository;
    private final ContentSearchFacade contentSearchFacade;
    private final LibraryService libraryService;
    private final DiaryFactory diaryFactory;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final Environment env;
    private final Rq rq;

    private static final String CACHE_KEY_PREFIX = "diaryDetail::";

    @Transactional
    public Diary writeWithContentSearch(DiaryWriteRequestDto req, User user) {
        // 로그인 사용자 필수 체크
        if (user == null) {
            throw new CustomException(ErrorCode.AUTH_UNAUTHORIZED);
        }

        // 외부 콘텐츠 검색
        ContentSearchResultDto result = contentSearchFacade.search(req.type(), req.externalId());
        if (result == null || result.externalId() == null) {
            throw new CustomException(ErrorCode.CONTENT_NOT_FOUND);
        }

        // 콘텐츠 저장 or 조회
        Content content = contentService.saveOrGet(result, req.type());

        // Diary 생성 (연관관계 포함)
        Diary diary = diaryFactory.create(user, content, req.title(), req.contentText(), req.rating(), req.isPublic(), req.tagNames(), result.genres(), req.ottIds());

        // 저장
        return diaryRepository.save(diary);
    }

    @Transactional
    public DiaryResponseDto update(int diaryId, DiaryUpdateRequestDto dto, User user) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(DiaryNotFoundException::new);

        // 작성자 검증
        if (!diary.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.AUTH_FORBIDDEN);
        }

        // 콘텐츠 변경 처리
        Content oldContent = diary.getContent();
        boolean contentChanged = !oldContent.getExternalId().equals(dto.externalId())
                || !oldContent.getType().equals(dto.type());

        if (contentChanged) {
            ContentSearchResultDto result = contentSearchFacade.search(dto.type(), dto.externalId());
            if (result == null || result.externalId() == null) {
                throw new CustomException(ErrorCode.CONTENT_NOT_FOUND);
            }
            Content newContent = contentService.saveOrGet(result, dto.type());
            diary.setContent(newContent);
            if (result.genres() != null) {
                diary.updateGenres(result.genres(), genreService, libraryService);
            }
        }

        diary.update(dto.title(), dto.contentText(), dto.rating(), dto.isPublic());
        diary.updateTags(dto.tagNames(), tagRepository);
        diary.updateOtts(dto.ottIds(), ottRepository);

        diaryRepository.flush();
        objectRedisTemplate.delete("diaryDetail::" + diaryId);

        return DiaryMapper.toResponseDto(diary);
    }

    public DiaryDetailDto getDiaryDetail(Integer diaryId) {
        String cacheKey = CACHE_KEY_PREFIX + diaryId;

        // 다이어리 조회
        Diary diary;
        if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
            diary = diaryRepository.findById(diaryId)
                    .orElseThrow(DiaryNotFoundException::new);
        } else {
            Object cached = objectRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.convertValue(cached, DiaryDetailDto.class);
            } else {
                diary = diaryRepository.findById(diaryId)
                        .orElseThrow(DiaryNotFoundException::new);
                DiaryDetailDto detailDto = DiaryDetailDto.of(diary);
                objectRedisTemplate.opsForValue().set(cacheKey, detailDto);
                return detailDto;
            }
        }

        // 비공개 글 접근 제한
        User currentUser = null;
        try {
            currentUser = rq.getCurrentUser();
        } catch (Exception ignored) {
        }

        if (!diary.getIsPublic() &&
                (currentUser == null || !diary.getUser().getId().equals(currentUser.getId()))) {
            throw new CustomException(ErrorCode.AUTH_FORBIDDEN);
        }

        return DiaryDetailDto.of(diary);
    }

    @Transactional
    @CacheEvict(value = "diaryDetail", key = "#diaryId")
    public void delete(int diaryId, User user) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));

        // 작성자 검증
        if (!diary.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.AUTH_FORBIDDEN);
        }

        diaryRepository.delete(diary);
    }

    public Page<DiaryResponseDto> getDiariesByUser(Integer userId, Pageable pageable) {
        Page<Diary> diaries = diaryRepository.findByUserId(userId, pageable);
        return diaries.map(DiaryMapper::toResponseDto);
    }
}
