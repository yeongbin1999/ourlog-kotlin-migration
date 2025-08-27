package com.back.ourlog.domain.like.controller;

import com.back.ourlog.domain.like.dto.LikeCountResponse;
import com.back.ourlog.domain.like.dto.LikeResponse;
import com.back.ourlog.domain.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
@Tag(name = "좋아요 API")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{diaryId}")
    @Operation(summary = "좋아요 등록")
    public ResponseEntity<LikeResponse> like(@PathVariable Integer diaryId) {
        boolean liked = likeService.like(diaryId);
        int likeCount = likeService.getLikeCount(diaryId);
        return ResponseEntity.ok(new LikeResponse(liked, likeCount));
    }

    @DeleteMapping("/{diaryId}")
    @Operation(summary = "좋아요 취소")
    public ResponseEntity<LikeResponse> unlike(@PathVariable Integer diaryId) {
        likeService.unlike(diaryId);
        int likeCount = likeService.getLikeCount(diaryId);
        return ResponseEntity.ok(new LikeResponse(false, likeCount));
    }

    @GetMapping("/count")
    @Operation(summary = "좋아요 수 단건 조회")
    public ResponseEntity<LikeCountResponse> getLikeCount(@RequestParam Integer diaryId){
        int count = likeService.getLikeCount(diaryId);
        return ResponseEntity.ok(new LikeCountResponse(count));
    }

}
