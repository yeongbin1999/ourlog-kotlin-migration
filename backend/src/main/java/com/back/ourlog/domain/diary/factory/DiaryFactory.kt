package com.back.ourlog.domain.diary.factory

import com.back.ourlog.domain.content.entity.Content
import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.genre.service.GenreService
import com.back.ourlog.domain.ott.repository.OttRepository
import com.back.ourlog.domain.tag.repository.TagRepository
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.external.library.service.LibraryService
import org.springframework.stereotype.Component

@Component
class DiaryFactory(
    private val tagRepository: TagRepository,
    private val genreService: GenreService,
    private val ottRepository: OttRepository,
    private val libraryService: LibraryService
) {
    fun create(
        user: User, content: Content,
        title: String, contentText: String, rating: Float, isPublic: Boolean,
        tagNames: List<String>, genreRawNames: List<String>?, ottIds: List<Int>
    ): Diary {
        val diary = Diary(user, content, title, contentText, rating, isPublic)

        // update 메서드 재사용으로 중복 제거
        diary.updateTags(tagNames, tagRepository)

        genreRawNames?.let { diary.updateGenres(genreRawNames, genreService, libraryService) }

        diary.updateOtts(ottIds, ottRepository)

        return diary
    }
}
