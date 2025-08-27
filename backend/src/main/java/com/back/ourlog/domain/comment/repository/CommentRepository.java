package com.back.ourlog.domain.comment.repository;

import com.back.ourlog.domain.comment.entity.Comment;
import com.back.ourlog.domain.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 특정 일기에 달린 댓글 수를 계산..
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    int countByDiaryId(Integer diaryId);

    // 댓글정보 - 최신 순 정렬 (최신 댓글 일수록 위에 배치)
    @Query("select c from Comment c where c.diary = :diary order by c.createdAt DESC")
    List<Comment> findByDiaryOrderByCreatedAtDesc(@Param("diary") Diary diary);
}
