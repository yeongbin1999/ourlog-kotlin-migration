package com.back.ourlog.domain.content.service;

import com.back.ourlog.domain.content.dto.ContentResponseDto;
import com.back.ourlog.domain.content.dto.ContentSearchResultDto;
import com.back.ourlog.domain.content.entity.Content;
import com.back.ourlog.domain.content.entity.ContentType;
import com.back.ourlog.domain.content.repository.ContentRepository;
import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final DiaryRepository diaryRepository;
    private final ContentRepository contentRepository;

    // 외부 API 연동하면 externalId, type 기준으로 정보 갱신하도록 수정
    public Content getOrCreateContent(String externalId, ContentType type) {
        return contentRepository.findByExternalIdAndType(externalId, type)
                .orElseGet(() -> {
                    Content content = new Content(
                            "제목 없음",
                            type,
                            "제작자",
                            null,
                            null,
                            LocalDateTime.now(),
                            externalId
                    );
                    return contentRepository.save(content);
                });
    }

    public ContentResponseDto getContent(int diaryId) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow();
        Content content = diary.getContent();

        return new ContentResponseDto(content);
    }

    public Content saveOrGet(ContentSearchResultDto result, ContentType type) {
        return contentRepository.findByExternalIdAndType(result.externalId(), type)
                .orElseGet(() -> contentRepository.save(Content.of(result)));
    }

}
