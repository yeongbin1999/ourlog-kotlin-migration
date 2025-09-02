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
import com.back.ourlog.domain.user.entity.User.Companion.createNormalUser
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

    private val random = kotlin.random.Random(42)

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

    private fun createTags(): MutableList<Tag> =
        tagRepository.saveAll(
            listOf("힐링", "감동", "로맨스", "스릴러", "코미디", "액션", "음악", "철학")
                .map { Tag(it) }
        )

    private fun createGenres(): MutableList<Genre> =
        genreRepository.saveAll(
            listOf("드라마", "SF", "판타지", "애니메이션", "다큐멘터리")
                .map { Genre(it) }
        )

    private fun createOtts(): MutableList<Ott> =
        ottRepository.saveAll(
            listOf("Netflix", "Disney+", "Prime Video", "TVING", "Watcha")
                .map {
                    Ott(it, "https://logo.com/${it.lowercase().replace("+", "")}")
                }
        )

    private fun createUsers(count: Int): MutableList<User> {
        val users = (1..count).map { i ->
            val email = "user$i@test.com"
            val password = passwordEncoder.encode("password$i")
            val nickname = "유저$i"
            val profile = "https://picsum.photos/200?random=$i"
            val bio = "안녕하세요! 저는 $nickname 입니다."
            createNormalUser(email, password, nickname, profile, bio)
        }
        return userRepository.saveAll(users)
    }

    private fun createContents(count: Int): MutableList<Content> =
        contentRepository.saveAll(
            (1..count).map { i ->
                Content(
                    title = "콘텐츠 $i",
                    type = ContentType.values()[random.nextInt(ContentType.values().size)],
                    creatorName = "제작자 ${(i % 5) + 1}",
                    description = "이것은 콘텐츠 $i 에 대한 설명입니다.",
                    posterUrl = "https://picsum.photos/300?content=$i",
                    releasedAt = LocalDateTime.now().minusDays(random.nextLong(1000)),
                    externalId = "EXT-$i"
                )
            }
        )

    private fun createDiaries(users: List<User>, contents: List<Content>, count: Int): MutableList<Diary> =
        diaryRepository.saveAll(
            (1..count).map {
                val user = users.random(random)
                val content = contents.random(random)
                Diary(
                    user = user,
                    content = content,
                    title = "다이어리 $it",
                    contentText = "이것은 다이어리 $it 의 본문 내용입니다.",
                    rating = (random.nextInt(5) + 1).toFloat(),
                    isPublic = random.nextBoolean()
                )
            }
        )

    private fun createFollows(users: List<User>) {
        val follows = mutableListOf<Follow>()
        users.forEach { follower ->
            val followCount = 3 + random.nextInt(3)
            val alreadyFollowed = mutableSetOf<User>()
            repeat(followCount) {
                val followee = users.random(random)
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

        diaries.forEach { diary ->
            repeat(2 + random.nextInt(3)) {
                val commenter = users.random(random)
                comments.add(Comment(diary, commenter, "이 다이어리 정말 좋네요! ${UUID.randomUUID()}"))
            }
            repeat(1 + random.nextInt(5)) {
                val liker = users.random(random)
                if (!likes.any { it.user == liker && it.diary == diary }) {
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
        diaries.forEach { diary ->
            diary.diaryTags.addAll(tags.shuffled(random).take(1 + random.nextInt(3)).map { DiaryTag(diary, it) })
            diary.diaryGenres.addAll(genres.shuffled(random).take(1 + random.nextInt(2)).map { DiaryGenre(diary, it) })
            diary.diaryOtts.addAll(otts.shuffled(random).take(1 + random.nextInt(2)).map { DiaryOtt(diary, it) })
        }
        diaryRepository.saveAll(diaries)
    }
}