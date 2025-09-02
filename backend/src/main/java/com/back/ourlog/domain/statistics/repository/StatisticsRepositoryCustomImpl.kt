package com.back.ourlog.domain.statistics.repository

import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.content.entity.QContent
import com.back.ourlog.domain.diary.entity.QDiary
import com.back.ourlog.domain.genre.entity.QDiaryGenre.Companion.diaryGenre
import com.back.ourlog.domain.genre.entity.QGenre.Companion.genre
import com.back.ourlog.domain.ott.entity.QDiaryOtt.Companion.diaryOtt
import com.back.ourlog.domain.ott.entity.QOtt.Companion.ott
import com.back.ourlog.domain.statistics.dto.*
import com.back.ourlog.domain.tag.entity.QDiaryTag
import com.back.ourlog.domain.tag.entity.QTag
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Value
import com.querydsl.core.types.dsl.StringTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class StatisticsRepositoryCustomImpl(
    private val em: EntityManager,
    private val queryFactory: JPAQueryFactory,
    @Value("\${app.sql.lineMonthly}") private val lineMonthlySql: String,
    @Value("\${app.sql.lineDaily}") private val lineDailySql: String
) : StatisticsRepositoryCustom {

    val diary = QDiary.diary
    val content = QContent.content
    val diaryTag = QDiaryTag.diaryTag
    val tag = QTag.tag

    val monthlyPeriodExpr: StringTemplate = Expressions.stringTemplate(lineMonthlySql, diary.createdAt)
    val dailyPeriodExpr: StringTemplate = Expressions.stringTemplate(lineDailySql, diary.createdAt)

    /** 총 다이어리 개수 */
    override fun getTotalDiaryCountByUserId(userId: Int): Long {

        return queryFactory
            .select(diary.count())
            .from(diary)
            .where(diary.user.id.eq(userId))
            .fetchOne() ?: 0L
    }

    /** 평균 별점 */
    override fun getAverageRatingByUserId(userId: Int): Double {

        return queryFactory
            .select(diary.rating.avg())
            .from(diary)
            .where(diary.user.id.eq(userId))
            .fetchOne().let {
                // 소수점 둘째 자리까지 반올림
                if (it != null) String.format("%.2f", it).toDouble() else 0.0
            }
    }

    /** 제일 많이 본 타입 및 개수 */
    override fun findFavoriteTypeAndCountByUserId(userId: Int): FavoriteTypeAndCountDto {
        val result = queryFactory
            .select(content.type, diary.count())
            .from(diary)
            .join(diary.content, content)
            .where(diary.user.id.eq(userId))
            .groupBy(content.type)
            .orderBy(diary.count().desc())
            .fetchFirst()

        return FavoriteTypeAndCountDto(
            favoriteType = result?.get(content.type)?.toString() ?: "없음",
            favoriteTypeCount = result?.get(diary.count()) ?: 0L
        )
    }

    /** 제일 느낀 감정(Tag) 및 개수 */
    override fun findFavoriteEmotionAndCountByUserId(userId: Int): FavoriteEmotionAndCountDto {

        val result = queryFactory
            .select(tag.name, diaryTag.count())
            .from(diaryTag)
            .join(diaryTag.diary, diary)
            .join(diaryTag.tag, tag)
            .where(diary.user.id.eq(userId))
            .groupBy(tag.name)
            .orderBy(diaryTag.count().desc())
            .fetchFirst()

        return FavoriteEmotionAndCountDto(
            favoriteEmotion = result?.get(tag.name) ?: "없음",
            favoriteEmotionCount = result?.get(diaryTag.count()) ?: 0L
        )
    }

    /** 최근 6개월 감상 수 */
    override fun count6MonthlyDiaryByUserId(userId: Int, startDate: LocalDateTime): List<MonthlyDiaryCount> {
        val result = queryFactory
            .select(monthlyPeriodExpr, diary.count())
            .from(diary)
            .where(
                diary.user.id.eq(userId),
                diary.createdAt.goe(startDate)
            )
            .groupBy(monthlyPeriodExpr)
            .orderBy(monthlyPeriodExpr.asc())
            .fetch()

        if (result.isEmpty()) return emptyList()

        return result.map { tuple ->
            MonthlyDiaryCount(
                tuple.get(monthlyPeriodExpr) ?: "없음",
                tuple.get(diary.count()) ?: 0L
            )
        }
    }

    /** 콘텐츠 타입별 감상 수 */
    override fun findTypeCountsByUserId(userId: Int): List<TypeCountDto> {

        val result = queryFactory
            .select(content.type, diary.count())
            .from(diary)
            .join(diary.content, content)
            .where(diary.user.id.eq(userId))
            .groupBy(content.type)
            .orderBy(diary.count().desc())
            .fetch()

        if (result.isEmpty()) return emptyList()

        return result.map { tuple ->
            TypeCountDto(
                tuple.get(content.type).toString(),
                tuple.get(diary.count()) ?: 0L
            )
        }
    }

    /** 콘텐츠 타입별 월별 추이 */
    override fun findTypeLineMonthly(userId: Int, start: LocalDateTime, end: LocalDateTime): List<TypeLineGraphDto> = findTypeLine(userId, start, end, monthlyPeriodExpr)
    /** 콘텐츠 타입별 일별 추이 */
    override fun findTypeLineDaily(userId: Int, start: LocalDateTime, end: LocalDateTime): List<TypeLineGraphDto> = findTypeLine(userId, start, end, dailyPeriodExpr)

    fun findTypeLine(userId: Int, start: LocalDateTime, end: LocalDateTime, periodExpr: StringTemplate): List<TypeLineGraphDto> {
        val result = queryFactory
            .select(Projections.constructor(
                TypeLineGraphDto::class.java,
                periodExpr,
                content.type,
                diary.count()
            ))
            .from(diary)
            .join(content).on(diary.content.id.eq(content.id))
            .where(
                diary.user.id.eq(userId),
                diary.createdAt.between(start, end)
            )
            .groupBy(periodExpr, content.type)
            .orderBy(periodExpr.asc(), content.type.asc())
            .fetch()

        return result
    }

    /** 콘텐츠 타입별 순위 */
    override fun findTypeRanking(userId: Int, start: LocalDateTime, end: LocalDateTime): List<TypeRankDto> {
        return queryFactory
            .select(Projections.constructor(
                TypeRankDto::class.java,
                content.type,        // 콘텐츠 타입
                diary.count()        // 해당 타입의 카운트
            ))
            .from(diary)
            .join(content).on(diary.content.id.eq(content.id))
            .where(
                diary.user.id.eq(userId),
                diary.createdAt.between(start, end)
            )
            .groupBy(content.type)
            .orderBy(diary.count().desc())
            .fetch()
    }

    /** 장르별 월별 추이 */
    override fun findGenreLineMonthly(userId: Int, start: LocalDateTime, end: LocalDateTime): List<GenreLineGraphDto> = findGenreLine(userId, start, end, monthlyPeriodExpr)
    /** 장르별 일별 추이 */
    override fun findGenreLineDaily(userId: Int, start: LocalDateTime, end: LocalDateTime): List<GenreLineGraphDto> = findGenreLine(userId, start, end, dailyPeriodExpr)

    fun findGenreLine(userId: Int, start: LocalDateTime, end: LocalDateTime, periodExpr: StringTemplate): List<GenreLineGraphDto> {
        val result = queryFactory
            .select(Projections.constructor(
                GenreLineGraphDto::class.java,
                periodExpr,
                genre.name,
                diary.count()
            ))
            .from(diary)
            .join(diaryGenre).on(diaryGenre.diary.id.eq(diary.id))
            .join(genre).on(genre.id.eq(diaryGenre.genre.id))
            .where(
                diary.user.id.eq(userId),
                diary.createdAt.between(start, end)
            )
            .groupBy(periodExpr, genre.name)
            .orderBy(periodExpr.asc(), genre.name.asc())
            .fetch()

        return result
    }

    /** 장르별 순위 */
    override fun findGenreRanking(userId: Int, start: LocalDateTime, end: LocalDateTime): List<GenreRankDto> {
        val result = queryFactory
            .select(Projections.constructor(
                GenreRankDto::class.java,
                genre.name,
                diary.count()
            ))
            .from(diary)
            .join(diaryGenre).on(diaryGenre.diary.id.eq(diary.id))
            .join(genre).on(genre.id.eq(diaryGenre.genre.id))
            .where(
                diary.user.id.eq(userId),
                diary.createdAt.between(start, end)
            )
            .groupBy(genre.name)
            .orderBy(diary.count().desc())
            .fetch()

        return result
    }

    /** 감정별 월별 추이 */
    override fun findEmotionLineMonthly(userId: Int, start: LocalDateTime, end: LocalDateTime): List<EmotionLineGraphDto> = findEmotionLine(userId, start, end, monthlyPeriodExpr)
    /** 감정별 일별 추이 */
    override fun findEmotionLineDaily(userId: Int, start: LocalDateTime, end: LocalDateTime): List<EmotionLineGraphDto> = findEmotionLine(userId, start, end, dailyPeriodExpr)

    fun findEmotionLine(userId: Int, start: LocalDateTime, end: LocalDateTime, periodExpr: StringTemplate): List<EmotionLineGraphDto> {
        return queryFactory
            .select(Projections.constructor(
                EmotionLineGraphDto::class.java,
                periodExpr,
                tag.name,
                diary.count()
            ))
            .from(diary)
            .join(diaryTag).on(diaryTag.diary.id.eq(diary.id))
            .join(tag).on(tag.id.eq(diaryTag.tag.id))
            .where(
                diary.user.id.eq(userId),
                diary.createdAt.between(start, end)
            )
            .groupBy(periodExpr, tag.name)
            .orderBy(periodExpr.asc(), tag.name.asc())
            .fetch()
    }

    /** 감정별 순위 */
    override fun findEmotionRanking(userId: Int, start: LocalDateTime, end: LocalDateTime): List<EmotionRankDto> {
        return queryFactory
            .select(Projections.constructor(
                EmotionRankDto::class.java,
                tag.name,
                diary.count()
            ))
            .from(diary)
            .join(diaryTag).on(diaryTag.diary.id.eq(diary.id))
            .join(tag).on(tag.id.eq(diaryTag.tag.id))
            .where(
                diary.user.id.eq(userId),
                diary.createdAt.between(start, end)
            )
            .groupBy(tag.name)
            .orderBy(diary.count().desc())
            .fetch()
    }

    /** OTT별 월별 추이 */
    override fun findOttLineMonthly(userId: Int, start: LocalDateTime, end: LocalDateTime): List<OttLineGraphDto> = findOttLine(userId, start, end, monthlyPeriodExpr)
    /** OTT별 일별 추이 */
    override fun findOttLineDaily(userId: Int, start: LocalDateTime, end: LocalDateTime): List<OttLineGraphDto> = findOttLine(userId, start, end, dailyPeriodExpr)

    private fun findOttLine(
        userId: Int,
        start: LocalDateTime,
        end: LocalDateTime,
        periodExpr: StringTemplate
    ): List<OttLineGraphDto> {
        return queryFactory
            .select(Projections.constructor(
                OttLineGraphDto::class.java,
                periodExpr,
                ott.name,
                diary.count()
            ))
            .from(diary)
            .join(diaryOtt).on(diaryOtt.diary.id.eq(diary.id))
            .join(ott).on(ott.id.eq(diaryOtt.ott.id))
            .where(
                diary.user.id.eq(userId),
                diary.createdAt.between(start, end)
            )
            .groupBy(periodExpr, ott.name)
            .orderBy(periodExpr.asc(), ott.name.asc())
            .fetch()
    }

    /** OTT별 순위 */
    override fun findOttRanking(userId: Int, start: LocalDateTime, end: LocalDateTime): List<OttRankDto> {
        return queryFactory
            .select(Projections.constructor(
                OttRankDto::class.java,
                ott.name,
                diary.count()
            ))
            .from(diary)
            .join(diaryOtt).on(diaryOtt.diary.id.eq(diary.id))
            .join(ott).on(ott.id.eq(diaryOtt.ott.id))
            .where(
                diary.user.id.eq(userId),
                diary.createdAt.between(start, end)
            )
            .groupBy(ott.name)
            .orderBy(diary.count().desc())
            .fetch()
    }
}
