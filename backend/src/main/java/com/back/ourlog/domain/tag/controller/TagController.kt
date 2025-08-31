package com.back.ourlog.domain.tag.controller

import com.back.ourlog.domain.tag.service.TagService
import com.back.ourlog.global.common.extension.toSuccessResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tags")
@Tag(name = "태그 API")
class TagController(
    private val tagService: TagService
) {

    @GetMapping
    @Operation(summary = "전체 태그 조회", description = "등록된 모든 태그를 조회합니다.")
    fun getAllTags() = tagService.getAllTags()
        .toSuccessResponse("전체 태그 조회 성공")
}
