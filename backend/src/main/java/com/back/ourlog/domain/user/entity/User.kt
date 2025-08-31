package com.back.ourlog.domain.user.entity

import com.back.ourlog.domain.banHistory.entity.BanHistory
import com.back.ourlog.domain.comment.entity.Comment
import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.follow.entity.Follow
import com.back.ourlog.domain.like.entity.Like
import com.back.ourlog.domain.user.entity.Role
import jakarta.persistence.*
import java.time.LocalDateTime
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["provider", "providerId"])
    ]
)
class User(
    @Column(unique = true, length = 50)
    var email: String,

    @Column(length = 100)
    var password: String? = null,

    @Column(nullable = false, length = 50)
    var nickname: String,

    var profileImageUrl: String? = null,

    var bio: String? = null,

    @Column(length = 20)
    var provider: String? = null,

    @Column(length = 100)
    var providerId: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @CreatedDate
    @Column(updatable = false)
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var role: Role = Role.USER

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var diaries: MutableList<Diary> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var comments: MutableList<Comment> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var likes: MutableList<Like> = mutableListOf()

    // 내가 팔로우 한 사람 (팔로잉)
    @OneToMany(mappedBy = "follower", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var followings: MutableList<Follow> = mutableListOf()

    // 나를 팔로우 한 사람 (팔로워)
    @OneToMany(mappedBy = "followee", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var followers: MutableList<Follow> = mutableListOf()

    @Column(nullable = false)
    var followingsCount: Int = 0

    @Column(nullable = false)
    var followersCount: Int = 0

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var banHistories: MutableList<BanHistory> = mutableListOf()

    // === Kotlin 전용 메서드 ===

    // 팔로워 카운트 증가
    fun increaseFollowersCount() {
        this.followersCount++
    }

    // 팔로워 카운트 감소 (0 이하로는 내려가지 않음)
    fun decreaseFollowersCount() {
        if (this.followersCount > 0) this.followersCount--
    }

    // 팔로잉 카운트 증가
    fun increaseFollowingsCount() {
        this.followingsCount++
    }

    // 팔로잉 카운트 감소 (0 이하로는 내려가지 않음)
    fun decreaseFollowingsCount() {
        if (this.followingsCount > 0) this.followingsCount--
    }

    // 댓글 삭제
    fun deleteComment(comment: Comment) {
        comments.remove(comment)
    }

    // 현재 제재 중인지 확인
    fun isCurrentlyBanned(): Boolean {
        // [규칙: Kotlin 컬렉션 API 활용]
        // Java Stream 대신 Kotlin의 any() 함수 사용
        return banHistories.any { it.isActiveNow }
    }

    companion object {
        // [규칙: Java 잔재 제거]
        // static 메서드를 companion object로 변경

        // === 일반 가입 전용 생성자 ===
        fun createNormalUser(
            email: String,
            encodedPassword: String,
            nickname: String,
            profileImageUrl: String? = null, // ✅ 수정 및 개선
            bio: String? = null              // ✅ 수정 및 개선
        ): User {
            return User(
                email = email,
                password = encodedPassword,
                nickname = nickname,
                profileImageUrl = profileImageUrl,
                bio = bio,
                provider = "local",
                providerId = email
            )
        }

        // === 소셜 가입 전용 생성자 ===
        fun createSocialUser(
            provider: String,
            providerId: String,
            email: String,
            nickname: String,
            profileImageUrl: String? = null // ✅ 수정 및 개선
        ): User {
            return User(
                provider = provider,
                providerId = providerId,
                email = email,
                nickname = nickname,
                profileImageUrl = profileImageUrl
            )
        }
    }
}