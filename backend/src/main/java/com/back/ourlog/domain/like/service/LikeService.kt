package com.back.ourlog.domain.like.service

import com.back.ourlog.domain.diary.repository.DiaryRepository
import com.back.ourlog.domain.like.entity.Like
import com.back.ourlog.domain.like.repository.LikeRepository
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import com.back.ourlog.global.rq.Rq
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikeService(
    private val likeRepository: LikeRepository,
    private val diaryRepository: DiaryRepository,
    private val rq: Rq,
) {

    @Transactional
    fun like(diaryId: Int): Boolean {
        val currentUser = rq.currentUser // rq.getCurrentUser() 대신 프로퍼티로 접근

        // takeIf를 사용해 코드를 더 간결하게 표현할 수 있습니다.
        if (likeRepository.existsByUserIdAndDiaryId(currentUser.id!!, diaryId)) {
            return false // 이미 좋아요를 눌렀으면 false 반환
        }

        val diary = diaryRepository.findByIdOrNull(diaryId)
            ?: throw CustomException(ErrorCode.DIARY_NOT_FOUND)

        likeRepository.save(Like(user = currentUser, diary = diary))
        return true
    }

    @Transactional
    fun unlike(diaryId: Int) {
        val currentUser = rq.currentUser
        likeRepository.deleteByUserIdAndDiaryId(currentUser.id!!, diaryId)
    }

    @Transactional(readOnly = true) // 데이터 변경이 없는 조회 메서드에는 readOnly = true 추가 (성능 최적화)
    fun getLikeCount(diaryId: Int): Int {
        return likeRepository.countByDiaryId(diaryId)
    }
}