package com.back.ourlog.domain.diary.query

import com.back.ourlog.domain.diary.repository.DiaryRepository
import jakarta.persistence.EntityManagerFactory
import org.assertj.core.api.Assertions
import org.hibernate.SessionFactory
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
class DiaryDetailQueryCountTest(
    @Autowired private val emf: EntityManagerFactory,
    @Autowired private val diaryRepository: DiaryRepository
) {
    @Test
    @Transactional(readOnly = true)
    @DisplayName("상세 조회는 단일 SELECT로 끝나야 한다")
    fun t1() {
        // 1) 테스트 대상 ID를 먼저 조회 (이 쿼리는 카운트에서 제외)
        val id = diaryRepository.findTopByOrderByIdDesc()?.id
            ?: error("테스트용 다이어리 데이터가 필요합니다.")

        // 2) 그 다음에 통계 초기화
        val stats = emf.unwrap(SessionFactory::class.java).statistics
        stats.clear()

        // 3) 대상 메서드 호출
        val d = diaryRepository.findWithAllById(id).orElseThrow()

        // 4) 컬렉션 실제 접근 (여기서 batch-size 로딩 발생)
        d.diaryTags.forEach { it.tag.name }     // fetch join → 추가쿼리 없음
        d.diaryGenres.forEach { it.genre.name } // LAZY + batch
        d.diaryOtts.forEach { it.ott.name }     // LAZY + batch

        // 5) 기대값: 본문 1 + (genres 2) + (otts 2) = 최대 5회
        val selects = stats.prepareStatementCount
        Assertions.assertThat(selects).isLessThanOrEqualTo(5)
    }
}