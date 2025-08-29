package com.back.ourlog.domain.tag.controller;

import com.back.ourlog.domain.tag.dto.TagResponse;
import com.back.ourlog.domain.tag.service.TagService;
import com.back.ourlog.global.common.dto.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "태그 API")
public class TagController {

    private final TagService tagService;

    @GetMapping
    @Operation(summary = "전체 태그 조회", description = "등록된 모든 태그를 조회합니다.")
    public RsData<List<TagResponse>> getAllTags() {
        List<TagResponse> tags = tagService.getAllTags();
        return RsData.of("200-1", "전체 태그 조회 성공", tags);
    }
}
