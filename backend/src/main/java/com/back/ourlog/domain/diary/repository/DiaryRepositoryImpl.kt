package com.back.ourlog.domain.diary.repository

import com.back.ourlog.domain.content.entity.QContent
import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.diary.entity.QDiary
import com.back.ourlog.domain.tag.entity.QDiaryTag
import com.back.ourlog.domain.tag.entity.QTag
import com.back.ourlog.domain.user.entity.QUser
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class DiaryRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : DiaryRepositoryCustom {

    override fun findPageByUserIdWithToOne(userId: Int, pageable: Pageable): Page<Diary> {
        val d = QDiary.diary
        val u = QUser.user
        val c = QContent.content

        val content = queryFactory
            .selectFrom(d)
            .join(d.user, u).fetchJoin()
            .join(d.content, c).fetchJoin()
            .where(u.id.eq(userId))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .applySort(pageable.sort, d)
            .fetch()

        val total = queryFactory
            .select(d.count())
            .from(d)
            .where(d.user.id.eq(userId))
            .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    override fun findPagePublicByUserIdWithToOne(userId: Int, pageable: Pageable): Page<Diary> {
        val d = QDiary.diary
        val u = QUser.user
        val c = QContent.content

        val content = queryFactory
            .selectFrom(d)
            .join(d.user, u).fetchJoin()
            .join(d.content, c).fetchJoin()
            .where(u.id.eq(userId).and(d.isPublic.isTrue))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .applySort(pageable.sort, d)
            .fetch()

        val total = queryFactory
            .select(d.count())
            .from(d)
            .where(d.user.id.eq(userId).and(d.isPublic.isTrue))
            .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    override fun findWithAllById(id: Int): Optional<Diary> {
        val d = QDiary.diary
        val u = QUser.user
        val c = QContent.content
        val dt = QDiaryTag.diaryTag
        val t = QTag.tag
        // val dg = QDiaryGenre.diaryGenre; val g = QGenre.genre
        // val doo = QDiaryOtt.diaryOtt;   val o = QOtt.ott

        val diary = queryFactory
            .selectFrom(d)
            .join(d.user, u).fetchJoin()
            .join(d.content, c).fetchJoin()

            // MultipleBagFetchException 방지: to-many 중 하나만 fetch join
            .leftJoin(d.diaryTags, dt).fetchJoin()
            .leftJoin(dt.tag, t).fetchJoin()

            // .leftJoin(d.diaryGenres, dg).fetchJoin().leftJoin(dg.genre, g).fetchJoin()
            // .leftJoin(d.diaryOtts, doo).fetchJoin().leftJoin(doo.ott, o).fetchJoin()
            .where(d.id.eq(id))
            .distinct()
            .fetchOne()

        return Optional.ofNullable(diary)
    }

    // Sort -> QueryDSL OrderSpecifier 매핑 (+ tie-breaker)
    private fun <T> JPAQuery<T>.applySort(sort: Sort, d: QDiary): JPAQuery<T> {
        var appliedAny = false
        if (sort.isUnsorted) {
            this.orderBy(OrderSpecifier(Order.DESC, d.createdAt))
            appliedAny = true
        } else {
            sort.forEach { o ->
                val direction = if (o.isAscending) Order.ASC else Order.DESC
                val expr = when (o.property) {
                    "id" -> d.id
                    "createdAt" -> d.createdAt
                    "updatedAt" -> d.updatedAt
                    "rating" -> d.rating
                    "title" -> d.title
                    else -> d.createdAt
                }
                this.orderBy(OrderSpecifier(direction, expr))
                appliedAny = true
            }
        }
        // 항상 안정적인 페이징을 위한 tie-breaker
        this.orderBy(OrderSpecifier(Order.DESC, d.id))
        return this
    }
}
