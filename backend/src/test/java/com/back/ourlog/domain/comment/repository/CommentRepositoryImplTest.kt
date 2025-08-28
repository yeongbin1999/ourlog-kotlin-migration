package com.back.ourlog.domain.comment.repository

import com.back.ourlog.domain.diary.repository.DiaryRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class CommentRepositoryImplTest {
    @Autowired
    private lateinit var commentRepository: CommentRepository

    @Autowired
    private lateinit var diaryRepository: DiaryRepository

    @Test
    @DisplayName("findQByDiaryOrderByCreatedAtDesc")
    @Transactional(readOnly = true)
    fun t1() {
        val diary = diaryRepository.findByIdOrNull(1)

        val comments = commentRepository.findQByDiaryOrderByCreatedAtDesc(diary!!)

        Assertions.assertThat(comments.get(0).id).isEqualTo(4)
    }
}