package com.back.ourlog.domain.comment.controller

import com.back.ourlog.domain.comment.dto.CommentRequestDto
import com.back.ourlog.domain.comment.dto.CommentResponseDto
import com.back.ourlog.domain.comment.dto.CommentUpdateRequestDto
import com.back.ourlog.domain.comment.service.CommentService
import com.back.ourlog.global.common.dto.RsData
import com.back.ourlog.global.common.extension.toSuccessResponse
import com.back.ourlog.global.common.extension.toSuccessResponseWithoutData
import com.back.ourlog.global.rq.Rq
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
@Tag(name = "댓글 API")
class CommentController(
    private val commentService: CommentService,
    private val rq: Rq
) {
    @PostMapping
    @Operation(summary = "댓글 등록")
    fun writeComment(@RequestBody @Valid req: CommentRequestDto): ResponseEntity<RsData<CommentResponseDto>> {
        val user = rq.currentUser

        val res = commentService.write(req.diaryId, user, req.content)

        return res.toSuccessResponse("댓글이 등록되었습니다.")
    }

    @GetMapping("/{diaryId}")
    @Operation(summary = "댓글 조회")
    fun getComments(@PathVariable("diaryId") diaryId: Int): ResponseEntity<RsData<List<CommentResponseDto>>> {
        val res = commentService.getComments(diaryId)

        return res.toSuccessResponse("${diaryId}번 다이어리 댓글이 조회되었습니다.")
    }

    @PutMapping
    @Operation(summary = "댓글 수정")
    fun updateComment(
        @RequestBody req: CommentUpdateRequestDto
    ): ResponseEntity<RsData<Nothing>> {
        val user = rq.currentUser

        commentService.checkCanUpdate(user, req.id)

        commentService.update(req.id, req.content)

        return toSuccessResponseWithoutData("${req.id}번 댓글이 수정되었습니다.")
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제")
    fun deleteComment(
        @PathVariable("commentId") commentId: Int
    ): ResponseEntity<RsData<Nothing>> {
        val user = rq.currentUser

        commentService.checkCanDelete(user, commentId)

        commentService.delete(commentId)

        return toSuccessResponseWithoutData("${commentId}번 댓글이 삭제되었습니다.")
    }
}