package com.back.ourlog.domain.timeline.dto

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.AllArgsConstructor
import lombok.Getter

// 타임라인에 띄울 데이터를 프론트에 전달할 때 사용..
data class TimelineResponse(
    val id: Int?,
    val title: String?,
    val content: String?,
    val createdAt: String?,
    val imageUrl: String?,
    val likeCount: Int,
    val commentCount: Int,

    // JSON으로 변환 시 'liked'가 아닌 'isLiked'로 나가도록 설정합니다.
    @field:JsonProperty("isLiked")
    val isLiked: Boolean,

    val user: UserSummary?
) {
    data class UserSummary(
        val id: Int?,
        val nickname: String?,
        val profileImageUrl: String?
    )
}