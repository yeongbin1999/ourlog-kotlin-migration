package com.back.ourlog.global.config

import com.back.ourlog.domain.comment.entity.Comment
import com.back.ourlog.domain.comment.repository.CommentRepository
import com.back.ourlog.domain.content.entity.Content
import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.content.repository.ContentRepository
import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.diary.repository.DiaryRepository
import com.back.ourlog.domain.follow.entity.Follow
import com.back.ourlog.domain.follow.repository.FollowRepository
import com.back.ourlog.domain.genre.entity.DiaryGenre
import com.back.ourlog.domain.genre.entity.Genre
import com.back.ourlog.domain.genre.repository.GenreRepository
import com.back.ourlog.domain.like.entity.Like
import com.back.ourlog.domain.like.repository.LikeRepository
import com.back.ourlog.domain.ott.entity.DiaryOtt
import com.back.ourlog.domain.ott.entity.Ott
import com.back.ourlog.domain.ott.repository.OttRepository
import com.back.ourlog.domain.tag.entity.DiaryTag
import com.back.ourlog.domain.tag.entity.Tag
import com.back.ourlog.domain.tag.repository.TagRepository
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.domain.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
@Profile("!prod")
class DataInitializer(
    private val userRepository: UserRepository,
    private val contentRepository: ContentRepository,
    private val diaryRepository: DiaryRepository,
    private val followRepository: FollowRepository,
    private val commentRepository: CommentRepository,
    private val likeRepository: LikeRepository,
    private val tagRepository: TagRepository,
    private val ottRepository: OttRepository,
    private val genreRepository: GenreRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    private val random = Random(42L) // 항상 동일한 랜덤 패턴

    @Transactional
    override fun run(vararg args: String?) {
        if (userRepository.count() > 0) {
            println("✅ 기존 데이터가 있어서 더미 데이터 생성을 건너뜁니다.")
            return
        }

        println("🚀 더미 데이터 생성 시작!")

        val tags = createTags()
        val genres = createGenres()
        val otts = createOtts()
        val users = createUsers(20)
        val contents = createContents(30)
        val diaries = createDiaries(users, contents, 50)
        createFollows(users)
        createCommentsAndLikes(users, diaries)
        attachTagsGenresOtts(diaries, tags, genres, otts)

        println("✅ 더미 데이터 생성 완료!")
    }

    private fun createTags(): List<Tag> =
        tagRepository.saveAll(listOf("힐링", "감동", "로맨스", "스릴러", "코미디", "액션", "음악", "철학").map { Tag(it) })

    private fun createGenres(): List<Genre> =
        genreRepository.saveAll(listOf("드라마", "SF", "판타지", "애니메이션", "다큐멘터리").map { Genre(it) })

    private fun createOtts(): List<Ott> =
        ottRepository.saveAll(listOf("Netflix", "Disney+", "Prime Video", "TVING", "Watcha")
            .map { Ott(it, "https://logo.com/${it.lowercase().replace("+", "")}") })

    private fun createUsers(count: Int): List<User> {
        val users = mutableListOf<User>()
        for (i in 1..count) {
            val email = "user$i@test.com"
            val password = passwordEncoder.encode("password$i")
            val nickname = "유저$i"
            val profile = "https://picsum.photos/200?random=$i"
            val bio = "안녕하세요! 저는 $nickname 입니다."
            users.add(User.createNormalUser(email, password, nickname, profile, bio))
        }
        return userRepository.saveAll(users)
    }

    private fun createContents(count: Int): List<Content> {
        val contents = mutableListOf<Content>()
        for (i in 1..count) {
            val title = "콘텐츠 $i"
            val type = ContentType.values()[random.nextInt(ContentType.values().size)]
            val creatorName = "제작자 ${i % 5 + 1}"
            val description = "이것은 $title 에 대한 설명입니다."
            val posterUrl = "https://picsum.photos/300?content=$i"
            val releasedAt = LocalDateTime.now().minusDays(random.nextInt(1000).toLong())
            val externalId = "EXT-$i"
            contents.add(Content(title, type, creatorName, description, posterUrl, releasedAt, externalId))
        }
        return contentRepository.saveAll(contents)
    }

    private fun createDiaries(users: List<User>, contents: List<Content>, count: Int): List<Diary> {
        val diaries = mutableListOf<Diary>()
        for (i in 1..count) {
            val user = users[random.nextInt(users.size)]
            val content = contents[random.nextInt(contents.size)]
            val title = "다이어리 $i"
            val contentText = "이것은 다이어리 $i 의 본문 내용입니다."
            val rating = random.nextInt(5) + 1f
            val isPublic = random.nextBoolean()
            diaries.add(Diary(user, content, title, contentText, rating, isPublic))
        }
        return diaryRepository.saveAll(diaries)
    }

    private fun createFollows(users: List<User>) {
        val follows = mutableListOf<Follow>()
        for (follower in users) {
            val followCount = 3 + random.nextInt(3)
            val alreadyFollowed = mutableSetOf<User>()
            repeat(followCount) {
                val followee = users[random.nextInt(users.size)]
                if (followee != follower && alreadyFollowed.add(followee)) {
                    follows.add(Follow(follower, followee))
                    follower.increaseFollowingsCount()
                    followee.increaseFollowersCount()
                }
            }
        }
        followRepository.saveAll(follows)
    }

    private fun createCommentsAndLikes(users: List<User>, diaries: List<Diary>) {
        val comments = mutableListOf<Comment>()
        val likes = mutableListOf<Like>()

        for (diary in diaries) {
            repeat(2 + random.nextInt(3)) {
                val commenter = users[random.nextInt(users.size)]
                comments.add(Comment(diary, commenter, "이 다이어리 정말 좋네요! ${UUID.randomUUID()}"))
            }

            val alreadyLiked = mutableSetOf<User>()
            repeat(1 + random.nextInt(5)) {
                val liker = users[random.nextInt(users.size)]
                if (alreadyLiked.add(liker)) {
                    likes.add(Like(liker, diary))
                }
            }
        }
        commentRepository.saveAll(comments)
        likeRepository.saveAll(likes)
    }

    private fun attachTagsGenresOtts(
        diaries: List<Diary>,
        tags: List<Tag>,
        genres: List<Genre>,
        otts: List<Ott>
    ) {
        for (diary in diaries) {
            diary.diaryTags.addAll(tags.shuffled(random).take(1 + random.nextInt(3)).map { DiaryTag(diary, it) })
            diary.diaryGenres.addAll(genres.shuffled(random).take(1 + random.nextInt(2)).map { DiaryGenre(diary, it) })
            diary.diaryOtts.addAll(otts.shuffled(random).take(1 + random.nextInt(2)).map { DiaryOtt(diary, it) })
        }
        diaryRepository.saveAll(diaries)
    }
}