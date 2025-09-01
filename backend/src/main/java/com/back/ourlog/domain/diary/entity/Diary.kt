package com.back.ourlog.domain.diary.entity

import com.back.ourlog.domain.comment.entity.Comment
import com.back.ourlog.domain.content.entity.Content
import com.back.ourlog.domain.genre.entity.DiaryGenre
import com.back.ourlog.domain.like.entity.Like
import com.back.ourlog.domain.ott.entity.DiaryOtt
import com.back.ourlog.domain.tag.entity.DiaryTag
import com.back.ourlog.domain.user.entity.User
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

    // 작성자 검증 헬퍼 (중복 제거용)
    fun me(userId: Int): Boolean = this.user.id == userId
    fun me(user: User): Boolean = this.user.id == user.id

    fun addComment(user: User, content: String): Comment {
        val comment = Comment(this, user, content)
        comments.add(comment)
        return comment
    }

    fun deleteComment(comment: Comment) {
        comments.remove(comment)
    }
}
