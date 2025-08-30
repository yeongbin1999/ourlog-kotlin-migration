package com.back.ourlog.domain.diary.dto

import com.back.ourlog.domain.content.entity.ContentType
import jakarta.validation.constraints.*

@JvmRecord
data class DiaryWriteRequestDto(
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
    @field:DecimalMax(
        value = "5.0",
        message = "평점은 5 이하이어야 합니다."
    )
    val rating: Float,

    @JvmField
    @field:NotNull
    val type: ContentType,

    @JvmField
    @field:NotBlank(message = "콘텐츠 식별자(externalId)는 필수입니다.")
    val externalId: String,

    // 역직렬화 오류 발생 따라서, emptyList로 초기값을 설정
    @JvmField
    @field:NotEmpty(message = "태그는 하나 이상 선택해야 합니다.")
    val tagNames: @NotEmpty List<String> = emptyList(),

    @JvmField
    val genreIds: List<Int> = emptyList(),

    @JvmField
    val ottIds: List<Int> = emptyList()
) {
    // 테스트용 생성자
    constructor(title: String, contentText: String) : this(
        title,
        contentText,
        true,
        4.5f,
        ContentType.MOVIE,
        "externalId",
        listOf("happy", "funny"),
        listOf(1, 2),
        listOf(1, 2)
    )
}
