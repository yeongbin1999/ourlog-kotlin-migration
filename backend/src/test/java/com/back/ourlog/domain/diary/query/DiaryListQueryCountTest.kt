package com.back.ourlog.domain.diary.query

import com.back.ourlog.domain.diary.repository.DiaryRepository
import jakarta.persistence.EntityManagerFactory
import org.assertj.core.api.Assertions
import org.hibernate.SessionFactory
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
class DiaryListQueryCountTest(
    @Autowired private val emf: EntityManagerFactory,
    @Autowired private val diaryRepository: DiaryRepository
) {
    @Test
    @Transactional(readOnly = true)
    @DisplayName("목록 조회 - to-one fetchJoin + batch-size로 N+1 억제")
    fun t1() {
        val stats = emf.unwrap(SessionFactory::class.java).statistics
        stats.clear()

        val userId = 1
        val page = diaryRepository.findPageByUserIdWithToOne(
            userId,
            PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt")))
        )

        // DTO 변환처럼 실제로 컬렉션 접근 유도 (여기서 batch-size가 작동)
        page.content.forEach { d ->
            d.diaryTags.forEach { it.tag.name }
            d.diaryGenres.forEach { it.genre.name }
            d.diaryOtts.forEach { it.ott.name }
        }

        val selects = stats.prepareStatementCount
        // 본문(1) + 카운트(1) + 컬렉션 로딩 몇 번(대개 3~6회) → 10 이하 권장
        Assertions.assertThat(selects).isLessThanOrEqualTo(10)
    }
}