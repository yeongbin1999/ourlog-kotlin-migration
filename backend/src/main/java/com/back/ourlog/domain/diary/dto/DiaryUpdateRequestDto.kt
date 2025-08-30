package com.back.ourlog.domain.diary.dto

import com.back.ourlog.domain.content.entity.ContentType
import jakarta.validation.constraints.*

// DiaryService 코틀린 전환 후 Jvm 속성 지워주기
@JvmRecord
data class DiaryUpdateRequestDto(
        @JvmField
        @field:NotBlank(message = "제목을 입력해주세요.")
        val title: String,

        @JvmField
        @field:NotBlank(message = "내용을 입력해주세요.")
        val contentText: String,

        @JvmField
        @field:NotNull
        val isPublic: Boolean,

        @JvmField
        @field:NotNull(message = "평점을 입력해주세요.")
        @field:DecimalMin(value = "0.0", message = "평점은 0 이상이어야 합니다.")
        @field:DecimalMax(value = "5.0", message = "평점은 5 이하이어야 합니다.")
        val rating: Float,

        @JvmField
        @field:NotNull
        val externalId: String,

        @JvmField
        @field:NotNull
        val type: ContentType,

        @JvmField
        @field:NotEmpty(message = "태그는 하나 이상 선택해야 합니다.")
        val tagNames: List<String>,

        @JvmField
        val genreIds: List<Int>,

        @JvmField
        val ottIds: List<Int>
)
