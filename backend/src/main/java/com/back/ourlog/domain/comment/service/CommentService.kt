package com.back.ourlog.domain.comment.service

import com.back.ourlog.domain.comment.dto.CommentResponseDto
import com.back.ourlog.domain.comment.entity.Comment
import com.back.ourlog.domain.comment.repository.CommentRepository
import com.back.ourlog.domain.diary.repository.DiaryRepository
import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.global.common.extension.findByIdOrThrow
import com.back.ourlog.global.common.extension.getOrThrow
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val diaryRepository: DiaryRepository,
    private val commentRepository: CommentRepository,
) {
    @Transactional
    fun write(diaryId: Int, user: User?, content: String): CommentResponseDto {
        val user = user.getOrThrow(ErrorCode.USER_NOT_FOUND)

        val diary = diaryRepository.findByIdOrThrow(diaryId, ErrorCode.DIARY_NOT_FOUND)

        val comment = diary.addComment(user, content)
        // 아이디 값을 넣기 위해 사용
        diaryRepository.flush()

        return CommentResponseDto(comment)
    }

    @Transactional(readOnly = true)
    fun getComments(diaryId: Int): List<CommentResponseDto> {
        val comments = commentRepository.findQByDiaryIdOrderByCreatedAtDesc(diaryId)
            // 댓글이 없는 경우 - 다이어리의 존재 여부 확인
            .takeIf { it.isNotEmpty() || diaryRepository.existsById(diaryId) }
            ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)

        // 최신 순으로 나열된 댓글 정보
        return comments
            .map { CommentResponseDto(it) }
    }

    @Transactional
    fun update(user: User?, id: Int, content: String) {
        val comment = commentRepository.findByIdOrThrow(id, ErrorCode.COMMENT_NOT_FOUND)

        checkCanAccess(user, comment, ErrorCode.COMMENT_UPDATE_FORBIDDEN)

        comment.update(content)
    }

    @Transactional
    fun delete(user: User?, id: Int) {
        val comment = commentRepository.findByIdOrThrow(id, ErrorCode.COMMENT_NOT_FOUND)

        checkCanAccess(user, comment, ErrorCode.COMMENT_DELETE_FORBIDDEN)

        // (Diary, User) 와 Comment 연관관계 제거
        comment.diary.deleteComment(comment)

        comment.user.deleteComment(comment)
    }

    // 유저가 접근(삭제 or 수정) 가능한지 여부 체크
    private fun checkCanAccess(user: User?, comment: Comment, errorCode: ErrorCode) {
        val user = user.getOrThrow(ErrorCode.USER_NOT_FOUND)

        comment.user.takeIf { it == user }
            ?:throw CustomException(errorCode)
    }
}
