package com.back.ourlog.domain.diary.dto

import com.back.ourlog.domain.content.entity.ContentType
import jakarta.validation.constraints.*

data class DiaryUpdateRequestDto(
        @field:NotBlank(message = "제목을 입력해주세요.")
        val title: String,

        @field:NotBlank(message = "내용을 입력해주세요.")
        val contentText: String,

        val isPublic: Boolean,

        @field:NotNull(message = "평점을 입력해주세요.")
        @field:DecimalMin(value = "0.0", message = "평점은 0 이상이어야 합니다.")
        @field:DecimalMax(value = "5.0", message = "평점은 5 이하이어야 합니다.")
        val rating: Float,

        @field:NotBlank
        val externalId: String,

        @field:NotNull
        val type: ContentType,

        @field:NotEmpty(message = "태그는 하나 이상 선택해야 합니다.")
        val tagNames: List<String>,

        // 업데이트에서 장르는 서버가 외부 API로 재추출 가능 → 선택
        val genreIds: List<Int> = emptyList(),

        // MOVIE 타입일 때만 사용
        val ottIds: List<Int> = emptyList()
)
