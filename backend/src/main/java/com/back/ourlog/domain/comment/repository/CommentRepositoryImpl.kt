package com.back.ourlog.domain.comment.repository

import com.back.ourlog.domain.comment.entity.Comment
import com.back.ourlog.domain.comment.entity.QComment
import com.back.ourlog.domain.user.entity.QUser
import com.querydsl.jpa.impl.JPAQueryFactory

class CommentRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CommentRepositoryCustom {
    override fun findQByDiaryIdOrderByCreatedAtDesc(diaryId: Int): List<Comment> {
        val comment = QComment.comment
        val user = QUser.user

        return queryFactory
            .selectFrom(comment)
            .join(comment.user, user).fetchJoin()
            .where(comment.diary.id.eq(diaryId))
            .orderBy(comment.createdAt.desc())
            .fetch()
    }
}