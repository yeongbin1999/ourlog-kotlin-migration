package com.back.ourlog.domain.diary.entity

import com.back.ourlog.domain.comment.entity.Comment
import com.back.ourlog.domain.content.entity.Content
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.genre.entity.DiaryGenre
import com.back.ourlog.domain.genre.service.GenreService
import com.back.ourlog.domain.like.entity.Like
import com.back.ourlog.domain.ott.entity.DiaryOtt
import com.back.ourlog.domain.ott.repository.OttRepository
import com.back.ourlog.domain.tag.entity.DiaryTag
import com.back.ourlog.domain.tag.entity.Tag
import com.back.ourlog.domain.tag.repository.TagRepository
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.external.library.service.LibraryService
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class Diary(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    var content: Content,

    var title: String,
    var contentText: String,
    var rating: Float,
    var isPublic: Boolean
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @CreatedDate
    @Column(updatable = false)
    lateinit var createdAt: LocalDateTime

    @LastModifiedDate
    lateinit var updatedAt: LocalDateTime

    @OneToMany(mappedBy = "diary", cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<Comment> = mutableListOf()

    @OneToMany(mappedBy = "diary", cascade = [CascadeType.ALL], orphanRemoval = true)
    var likes: MutableList<Like> = mutableListOf()

    @OneToMany(mappedBy = "diary", cascade = [CascadeType.ALL], orphanRemoval = true)
    var diaryTags: MutableList<DiaryTag> = mutableListOf()

    @OneToMany(mappedBy = "diary", cascade = [CascadeType.ALL], orphanRemoval = true)
    var diaryGenres: MutableList<DiaryGenre> = mutableListOf()

    @OneToMany(mappedBy = "diary", cascade = [CascadeType.ALL], orphanRemoval = true)
    var diaryOtts: MutableList<DiaryOtt> = mutableListOf()

    fun update(title: String, contentText: String, rating: Float, isPublic: Boolean) {
        this.title = title
        this.contentText = contentText
        this.rating = rating
        this.isPublic = isPublic
    }

    fun updateTags(newTagNames: List<String>, tagRepository: TagRepository) {
        val currentNames = diaryTags.map { it.tag.name }
        val toRemove = diaryTags.filter { it.tag.name !in newTagNames }
        diaryTags.removeAll(toRemove)

        val toAdd = newTagNames.filter { it !in currentNames }
        toAdd.forEach { tagName ->
            val tag = tagRepository.findByName(tagName) ?: tagRepository.save(Tag(tagName))
            diaryTags.add(DiaryTag(this, tag))
        }
    }

    fun updateGenres(
        newGenreNames: List<String>,
        genreService: GenreService,
        libraryService: LibraryService
    ) {
        val currentNames = diaryGenres.map { it.genre.name }
        val mappedNames = newGenreNames.map {
            if (content.type == ContentType.BOOK)
                libraryService.mapKdcToGenre(it)
            else it
        }

        val toRemove = diaryGenres.filter { it.genre.name !in mappedNames }
        diaryGenres.removeAll(toRemove)

        val toAdd = mappedNames.filter { it !in currentNames }
        toAdd.forEach { name ->
            val genre = genreService.findOrCreateByName(name)
            diaryGenres.add(DiaryGenre(this, genre))
        }
    }

    fun updateOtts(newOttIds: List<Int>, ottRepository: OttRepository) {
        if (content.type != ContentType.MOVIE) {
            diaryOtts.clear()
            return
        }

        diaryOtts.clear()

        newOttIds.forEach { ottId ->
            val ott = ottRepository.findById(ottId).orElseThrow {
                CustomException(ErrorCode.OTT_NOT_FOUND)
            }
            diaryOtts.add(DiaryOtt(this, ott))
        }
    }

    fun addComment(user: User, content: String): Comment {
        val comment = Comment(this, user, content)
        comments.add(comment)
        return comment
    }

    fun deleteComment(comment: Comment) {
        comments.remove(comment)
    }
}
