package com.back.ourlog.domain.diary.factory;

import com.back.ourlog.domain.content.entity.Content;
import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.genre.service.GenreService;
import com.back.ourlog.domain.ott.repository.OttRepository;
import com.back.ourlog.domain.tag.repository.TagRepository;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.external.library.service.LibraryService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DiaryFactory {

    private final TagRepository tagRepository;
    private final GenreService genreService;
    private final OttRepository ottRepository;
    private final LibraryService libraryService;

    public Diary create(User user, Content content,
                        String title, String contentText, Float rating, Boolean isPublic,
                        List<String> tagNames, List<String> genreRawNames, List<Integer> ottIds) {

        Diary diary = new Diary(user, content, title, contentText, rating, isPublic);

        // update 메서드 재사용으로 중복 제거
        diary.updateTags(tagNames, tagRepository);
        if (genreRawNames != null) {
            diary.updateGenres(genreRawNames, genreService, libraryService);
        }
        diary.updateOtts(ottIds, ottRepository);

        return diary;
    }
}
