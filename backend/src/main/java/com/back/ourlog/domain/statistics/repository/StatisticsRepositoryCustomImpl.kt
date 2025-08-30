package com.back.ourlog.domain.statistics.repository

import com.back.ourlog.domain.content.entity.ContentType
import com.back.ourlog.domain.content.entity.QContent
import com.back.ourlog.domain.diary.entity.QDiary
import com.back.ourlog.domain.statistics.dto.*
import com.back.ourlog.domain.tag.entity.QDiaryTag
import com.back.ourlog.domain.tag.entity.QDiaryTag.Companion.diaryTag
import com.back.ourlog.domain.tag.entity.QTag
import com.back.ourlog.domain.tag.entity.QTag.Companion.tag
import com.querydsl.jpa.JPAExpressions.select
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Value
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


    /** 총 다이어리 개수 */
    override fun getTotalDiaryCountByUserId(userId: Int): Long {

        return queryFactory
            .select(diary.count())
            .from(diary)
            .where(diary.user.id.eq(userId))
            .fetchOne() ?: 0L                  // 결과가 없을 때 안전 장치
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
            .limit(1)
            .fetch()
            .firstOrNull()

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
            .limit(1)
            .fetch()
            .firstOrNull()

        return FavoriteEmotionAndCountDto(
            favoriteEmotion = result?.get(tag.name) ?: "없음",
            favoriteEmotionCount = result?.get(diaryTag.count()) ?: 0L
        )
    }

    override fun count6MonthlyDiaryByUserId(userId: Int?, startDate: LocalDateTime): List<MonthlyDiaryCount> {
        val sql =
            "SELECT $lineMonthlySql AS period, COUNT(*) AS views " +
                "FROM diary d " +
                "WHERE user_id = ? AND created_at >= ? " +
                "GROUP BY period " +
                "ORDER BY period ASC"

        val query = em.createNativeQuery(sql)
        query.setParameter(1, userId)
        query.setParameter(2, startDate)

        val result = query.resultList as List<Array<Any>>

        return result.map { row ->
            MonthlyDiaryCount(
                row[0] as String,
                (row[1] as Number).toLong()
            )
        }
    }

    override fun findTypeCountsByUserId(userId: Int?): List<TypeCountDto>? {
        val sql =
            "SELECT c.type AS type, COUNT(*) AS count " +
                "FROM diary d " +
                "JOIN content c ON d.content_id = c.id " +
                "WHERE d.user_id = ? " +
                "GROUP BY c.type " +
                "ORDER BY count DESC"

        val query = em.createNativeQuery(sql)
        query.setParameter(1, userId)

        val result = query.resultList as List<Array<Any>>

        return if (result.isNotEmpty()) {
            result.map { row ->
                TypeCountDto(
                    row[0] as String,
                    (row[1] as Number).toLong()
                )
            }
        } else {
            null
        }
    }

    override fun findTypeLineMonthly(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<TypeLineGraphDto> {
        val sql =
            "SELECT $lineMonthlySql AS axisLabel, " +
                "c.type AS type, " +
                "COUNT(*) AS count " +
                "FROM diary d " +
                "JOIN content c ON d.content_id = c.id " +
                "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                "GROUP BY axisLabel, c.type " +
                "ORDER BY axisLabel, c.type"

        val query = em.createNativeQuery(sql)
        query.setParameter(1, userId)
        query.setParameter(2, start)
        query.setParameter(3, end)

        val result = query.resultList as List<Array<Any>>

        return result.map { row ->
            TypeLineGraphDto(
                row[0] as String,
                ContentType.valueOf(row[1] as String),
                (row[2] as Number).toLong()
            )
        }
    }

    override fun findTypeLineDaily(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<TypeLineGraphDto> {
        val sql =
            "SELECT $lineDailySql AS axisLabel, " +
                "c.type AS type, " +
                "COUNT(*) AS count " +
                "FROM diary d " +
                "JOIN content c ON d.content_id = c.id " +
                "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                "GROUP BY axisLabel, c.type " +
                "ORDER BY axisLabel, c.type"

        val query = em.createNativeQuery(sql)
        query.setParameter(1, userId)
        query.setParameter(2, start)
        query.setParameter(3, end)

        val result = query.resultList as List<Array<Any>>

        return result.map { row ->
            TypeLineGraphDto(
                row[0] as String,
                ContentType.valueOf(row[1] as String),
                (row[2] as Number).toLong()
            )
        }
    }

    override fun findTypeRanking(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<TypeRankDto> {
        val jpql =
            "select new com.back.ourlog.domain.statistics.dto.TypeRankDto(" +
                "c.type, count(d)" +
                ") " +
                "from Diary d join d.content c " +
                "where d.user.id = :uid and d.createdAt between :s and :e " +
                "group by c.type order by count(d) desc"

        return em.createQuery(jpql, TypeRankDto::class.java)
            .setParameter("uid", userId)
            .setParameter("s", start)
            .setParameter("e", end)
            .resultList
    }

    override fun findGenreLineMonthly(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<GenreLineGraphDto> {
        val sql =
            "SELECT $lineMonthlySql AS axisLabel, " +
                "g.name AS genre, " +
                "COUNT(*) AS cnt " +
                "FROM diary d " +
                "JOIN diary_genre dg ON d.id = dg.diary_id " +
                "JOIN genre g ON dg.genre_id = g.id " +
                "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                "GROUP BY axisLabel, g.name " +
                "ORDER BY axisLabel, g.name"

        val query = em.createNativeQuery(sql)
        query.setParameter(1, userId)
        query.setParameter(2, start)
        query.setParameter(3, end)

        val result = query.resultList as List<Array<Any>>

        return result.map { r ->
            GenreLineGraphDto(
                r[0] as String,
                r[1] as String,
                (r[2] as Number).toLong()
            )
        }
    }

    override fun findGenreLineDaily(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<GenreLineGraphDto> {
        val sql =
            "SELECT $lineDailySql AS axisLabel, " +
                "g.name AS genre, " +
                "COUNT(*) AS cnt " +
                "FROM diary d " +
                "JOIN diary_genre dg ON d.id = dg.diary_id " +
                "JOIN genre g ON dg.genre_id = g.id " +
                "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                "GROUP BY axisLabel, g.name " +
                "ORDER BY axisLabel, g.name"

        val query = em.createNativeQuery(sql)
        query.setParameter(1, userId)
        query.setParameter(2, start)
        query.setParameter(3, end)

        val result = query.resultList as List<Array<Any>>

        return result.map { r ->
            GenreLineGraphDto(
                r[0] as String,
                r[1] as String,
                (r[2] as Number).toLong()
            )
        }
    }

    override fun findGenreRanking(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<GenreRankDto> {
        val sql =
            "SELECT g.name AS genre, COUNT(*) AS totalCount " +
                "FROM diary d " +
                "JOIN diary_genre dg ON d.id = dg.diary_id " +
                "JOIN genre g ON dg.genre_id = g.id " +
                "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                "GROUP BY g.name ORDER BY totalCount DESC"

        val query = em.createNativeQuery(sql)
        query.setParameter(1, userId)
        query.setParameter(2, start)
        query.setParameter(3, end)

        val result = query.resultList as List<Array<Any>>

        return result.map { r ->
            GenreRankDto(
                r[0] as String,
                (r[1] as Number).toLong()
            )
        }
    }

    override fun findEmotionLineMonthly(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<EmotionLineGraphDto> {
        val sql =
            "SELECT $lineMonthlySql AS axisLabel, " +
                "t.name AS emotion, " +
                "COUNT(*) AS cnt " +
                "FROM diary d " +
                "JOIN diary_tag dt ON d.id = dt.diary_id " +
                "JOIN tag t ON dt.tag_id = t.id " +
                "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                "GROUP BY axisLabel, t.name " +
                "ORDER BY axisLabel, t.name"

        val query = em.createNativeQuery(sql)
        query.setParameter(1, userId)
        query.setParameter(2, start)
        query.setParameter(3, end)

        val result = query.resultList as List<Array<Any>>

        return result.map { r ->
            EmotionLineGraphDto(
                r[0] as String,
                r[1] as String,
                (r[2] as Number).toLong()
            )
        }
    }

    override fun findEmotionLineDaily(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<EmotionLineGraphDto> {
        val sql =
            "SELECT $lineDailySql AS axisLabel, " +
                "t.name AS emotion, " +
                "COUNT(*) AS cnt " +
                "FROM diary d " +
                "JOIN diary_tag dt ON d.id = dt.diary_id " +
                "JOIN tag t ON dt.tag_id = t.id " +
                "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                "GROUP BY axisLabel, t.name " +
                "ORDER BY axisLabel, t.name"

        val query = em.createNativeQuery(sql)
        query.setParameter(1, userId)
        query.setParameter(2, start)
        query.setParameter(3, end)

        val result = query.resultList as List<Array<Any>>

        return result.map { r ->
            EmotionLineGraphDto(
                r[0] as String,
                r[1] as String,
                (r[2] as Number).toLong()
            )
        }
    }

    override fun findEmotionRanking(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<EmotionRankDto> {
        val sql =
            "SELECT t.name AS emotion, COUNT(*) AS totalCount " +
                "FROM diary d " +
                "JOIN diary_tag dt ON d.id = dt.diary_id " +
                "JOIN tag t ON dt.tag_id = t.id " +
                "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                "GROUP BY t.name ORDER BY totalCount DESC"

        val query = em.createNativeQuery(sql)
        query.setParameter(1, userId)
        query.setParameter(2, start)
        query.setParameter(3, end)

        val result = query.resultList as List<Array<Any>>

        return result.map { r ->
            EmotionRankDto(
                r[0] as String,
                (r[1] as Number).toLong()
            )
        }
    }

    override fun findOttLineMonthly(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<OttLineGraphDto> {
        val sql =
            "SELECT $lineMonthlySql AS axisLabel, o.name AS ottName, COUNT(*) AS cnt " +
                "FROM diary d " +
                "JOIN diary_ott do ON d.id = do.diary_id " +
                "JOIN ott o ON do.ott_id = o.id " +
                "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                "GROUP BY axisLabel, o.name ORDER BY axisLabel, o.name"

        val query = em.createNativeQuery(sql)
        query.setParameter(1, userId)
        query.setParameter(2, start)
        query.setParameter(3, end)

        val result = query.resultList as List<Array<Any>>

        return result.map { r ->
            OttLineGraphDto(
                r[0] as String,
                r[1] as String,
                (r[2] as Number).toLong()
            )
        }
    }

    override fun findOttLineDaily(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<OttLineGraphDto> {
        val sql =
            "SELECT $lineDailySql AS axisLabel, o.name AS ottName, COUNT(*) AS cnt " +
                "FROM diary d " +
                "JOIN diary_ott do ON d.id = do.diary_id " +
                "JOIN ott o ON do.ott_id = o.id " +
                "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                "GROUP BY axisLabel, o.name ORDER BY axisLabel, o.name"

        val query = em.createNativeQuery(sql)
        query.setParameter(1, userId)
        query.setParameter(2, start)
        query.setParameter(3, end)

        val result = query.resultList as List<Array<Any>>

        return result.map { r ->
            OttLineGraphDto(
                r[0] as String,
                r[1] as String,
                (r[2] as Number).toLong()
            )
        }
    }

    override fun findOttRanking(userId: Int?, start: LocalDateTime, end: LocalDateTime): List<OttRankDto> {
        val sql =
            "SELECT o.name AS ottName, COUNT(*) AS totalCnt " +
                "FROM diary d " +
                "JOIN diary_ott do ON d.id = do.diary_id " +
                "JOIN ott o ON do.ott_id = o.id " +
                "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                "GROUP BY o.name ORDER BY totalCnt DESC"

        val query = em.createNativeQuery(sql)
        query.setParameter(1, userId)
        query.setParameter(2, start)
        query.setParameter(3, end)

        val result = query.resultList as List<Array<Any>>

        return result.map { r ->
            OttRankDto(
                r[0] as String,
                (r[1] as Number).toLong()
            )
        }
    }
}
