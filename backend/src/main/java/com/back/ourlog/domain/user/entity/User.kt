package com.back.ourlog.domain.user.entity

import com.back.ourlog.domain.banHistory.entity.BanHistory
import com.back.ourlog.domain.comment.entity.Comment
import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.follow.entity.Follow
import com.back.ourlog.domain.like.entity.Like
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["provider", "providerId"])
    ]
)
class User(
    @Column(unique = true, length = 50, nullable = false)
    val email: String,

    @Column(nullable = false, length = 50)
    var nickname: String,

    @Column(nullable = false, length = 100)
    var password: String,

    @Column(nullable = false, length = 20)
    var provider: String = "local",

    @Column(nullable = false, length = 100)
    var providerId: String = email,

    var profileImageUrl: String? = null,
    var bio: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0

    @CreatedDate
    @Column(updatable = false)
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var role: Role = Role.USER

    var followingsCount: Int = 0
    var followersCount: Int = 0

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var diaries: MutableList<Diary> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var comments: MutableList<Comment> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var likes: MutableList<Like> = mutableListOf()

    @OneToMany(mappedBy = "follower", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var followings: MutableList<Follow> = mutableListOf()

    @OneToMany(mappedBy = "followee", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var followers: MutableList<Follow> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var banHistories: MutableList<BanHistory> = mutableListOf()

    /**
     * 팔로워 수 1 증가
     * - 다른 사용자가 나를 팔로우할 때 호출
     * - 내 팔로워 수(followersCount)가 1 증가
     */
    fun increaseFollowersCount() = followersCount++

    /**
     * 팔로워 수 1 감소
     * - 다른 사용자가 나를 언팔로우할 때 호출
     * - 내 팔로워 수(followersCount)가 1 감소, 0 미만 방지
     */
    fun decreaseFollowersCount() { followersCount = (followersCount - 1).coerceAtLeast(0) }

    /**
     * 내가 팔로잉 하는 사람 수 1 증가
     * - 내가 다른 사용자를 팔로우할 때 호출
     * - 내가 팔로잉하는 사람 수(followingsCount)가 1 증가
     */
    fun increaseFollowingsCount() = followingsCount++

    /**
     * 내가 팔로잉 하는 사람 수 1 감소
     * - 내가 다른 사용자를 언팔로우할 때 호출
     * - 내가 팔로잉하는 사람 수(followingsCount)가 1 감소, 0 미만 방지
     */
    fun decreaseFollowingsCount() { followingsCount = (followingsCount - 1).coerceAtLeast(0) }

    fun deleteComment(comment: Comment) = comments.remove(comment)

    fun isCurrentlyBanned() = banHistories.any { it.isActiveNow }

    companion object {

        fun createNormalUser(
            email: String,
            encodedPassword: String,
            nickname: String,
            profileImageUrl: String? = null,
            bio: String? = null
        ) = User(
            email = email,
            password = encodedPassword,
            nickname = nickname,
            provider = "local",
            providerId = email,
            profileImageUrl = profileImageUrl,
            bio = bio
        )

        fun createSocialUser(
            provider: String,
            providerId: String,
            email: String,
            nickname: String,
            profileImageUrl: String? = null,
            bio: String? = null
        ) = User(
            email = email,
            password = "",
            nickname = nickname,
            provider = provider,
            providerId = providerId,
            profileImageUrl = profileImageUrl,
            bio = bio
        )
    }
}