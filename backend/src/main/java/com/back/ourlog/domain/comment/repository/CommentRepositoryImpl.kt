package com.back.ourlog.domain.comment.repository

import com.back.ourlog.domain.comment.entity.Comment
import com.back.ourlog.domain.comment.entity.QComment
import com.back.ourlog.domain.diary.entity.Diary
import com.querydsl.jpa.impl.JPAQueryFactory

class CommentRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CommentRepositoryCustom {
    override fun findQByDiaryOrderByCreatedAtDesc(diary: Diary): List<Comment> {
        val comment = QComment.comment

        return queryFactory
            .selectFrom(comment)
            .where(comment.diary.id.eq(diary.id))
            .orderBy(comment.createdAt.desc())
            .fetch()
    }
}