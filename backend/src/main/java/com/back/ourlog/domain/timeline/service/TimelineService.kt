package com.back.ourlog.domain.timeline.service

import com.back.ourlog.domain.comment.repository.CommentRepository
import com.back.ourlog.domain.like.repository.LikeRepository
import com.back.ourlog.domain.timeline.dto.TimelineResponse
import com.back.ourlog.domain.timeline.repository.TimelineRepository
import com.back.ourlog.global.rq.Rq
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 공개된 일기 목록 조회, 좋아요 수, 좋아요 여부, 댓글 수, 유저 정보 DTO -> 프론트 전달..
@Service
@Transactional(readOnly = true)
class TimelineService(
    private val timelineRepository: TimelineRepository,
    private val likeRepository: LikeRepository,
    private val commentRepository: CommentRepository,
    private val rq: Rq
) {

    fun getPublicTimeline(): List<TimelineResponse> {
        // 1. runCatching을 사용해 로그인 상태와 비로그인 상태를 안전하게 처리
        val currentUserId = runCatching { rq.currentUser.id }.getOrNull()
        val diaries = timelineRepository.findPublicDiaries()

        // 2. Java의 Stream 대신 코틀린 컬렉션 API map 사용
        return diaries.map { diary ->
            // 3. isLiked 여부를 ?.let과 ?: 엘비스 연산자로 안전하고 간결하게 처리
            val isLiked = currentUserId?.let { userId ->
                likeRepository.existsByUserIdAndDiaryId(userId, diary.id)
            } ?: false

            // 4. data class 생성 시 named arguments를 사용하여 가독성 향상
            TimelineResponse(
                id = diary.id,
                title = diary.title,
                content = diary.contentText,
                createdAt = diary.createdAt.toString(),
                imageUrl = diary.content.posterUrl,
                likeCount = likeRepository.countByDiaryId(diary.id),
                commentCount = commentRepository.countByDiaryId(diary.id),
                isLiked = isLiked,
                user = TimelineResponse.UserSummary(
                    id = diary.user.id,
                    nickname = diary.user.nickname,
                    profileImageUrl = diary.user.profileImageUrl
                )
            )
        }
    }
}