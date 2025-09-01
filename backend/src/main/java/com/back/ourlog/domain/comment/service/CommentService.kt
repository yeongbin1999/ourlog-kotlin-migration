package com.back.ourlog.domain.comment.service

import com.back.ourlog.domain.comment.dto.CommentResponseDto
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
        val diary = diaryRepository.findByIdOrThrow(diaryId, ErrorCode.DIARY_NOT_FOUND)

        // 최신 순으로 나열된 댓글 정보
        return commentRepository.findQByDiaryOrderByCreatedAtDesc(diary)
            .map { CommentResponseDto(it) }
    }

    @Transactional
    fun update(id: Int, content: String) {
        val comment = commentRepository.findByIdOrThrow(id, ErrorCode.COMMENT_NOT_FOUND)

        comment.update(content)
    }

    @Transactional
    fun delete(id: Int) {
        val comment = commentRepository.findByIdOrThrow(id, ErrorCode.COMMENT_NOT_FOUND)

        // (Diary, User) 와 Comment 연관관계 제거
        comment.diary.deleteComment(comment)

        comment.user.deleteComment(comment)
    }

    @Transactional(readOnly = true)
    fun checkCanDelete(user: User?, commentId: Int) {
        val user = user.getOrThrow(ErrorCode.USER_NOT_FOUND)

        val comment = commentRepository.findByIdOrThrow(commentId, ErrorCode.COMMENT_NOT_FOUND)

        comment.user.takeIf { it == user }
            ?: throw CustomException(ErrorCode.COMMENT_DELETE_FORBIDDEN)
    }

    @Transactional(readOnly = true)
    fun checkCanUpdate(user: User?, commentId: Int) {
        val user = user.getOrThrow(ErrorCode.USER_NOT_FOUND)

        val comment = commentRepository.findByIdOrThrow(commentId, ErrorCode.COMMENT_NOT_FOUND)

        comment.user.takeIf { it == user }
            ?: throw CustomException(ErrorCode.COMMENT_UPDATE_FORBIDDEN)
    }
}
