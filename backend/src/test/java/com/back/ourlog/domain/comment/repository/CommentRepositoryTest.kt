package com.back.ourlog.domain.comment.repository

import com.back.ourlog.domain.diary.repository.DiaryRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
class CommentRepositoryTest @Autowired constructor(
    private val diaryRepository: DiaryRepository,
    private val commentRepository: CommentRepository,
) {
    @Test
    @DisplayName("댓글 최신 순으로 나열")
    @Transactional(readOnly = true)
    fun t1() {
        val comments = commentRepository.findQByDiaryIdOrderByCreatedAtDesc(1)

        Assertions.assertThat(comments[0].createdAt).isAfter(comments[1].createdAt)
    }
}
