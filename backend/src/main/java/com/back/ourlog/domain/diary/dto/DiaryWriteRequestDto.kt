package com.back.ourlog.domain.diary.dto

import com.back.ourlog.domain.content.entity.ContentType
import jakarta.validation.constraints.*

data class DiaryWriteRequestDto(
    @field:NotBlank(message = "제목을 입력해주세요.")
    val title: String,

    @field:NotBlank(message = "내용을 입력해주세요.")
    val contentText: String,

    val isPublic: Boolean,

    @field:NotNull(message = "평점을 입력해주세요.")
    @field:DecimalMin(value = "0.0", message = "평점은 0 이상이어야 합니다.")
    @field:DecimalMax(value = "5.0", message = "평점은 5 이하이어야 합니다.")
    val rating: Float,

    @field:NotNull
    val type: ContentType,

    @field:NotBlank(message = "콘텐츠 식별자(externalId)는 필수입니다.")
    val externalId: String,

    // 역직렬화 기본값은 주되, 검증은 NotEmpty로 강제
    @field:NotEmpty(message = "태그는 하나 이상 선택해야 합니다.")
    val tagNames: List<String> = emptyList(),

    val genreIds: List<Int> = emptyList(),

    val ottIds: List<Int> = emptyList()
)
