package com.back.ourlog.domain.like.repository;

import com.back.ourlog.domain.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;


public interface LikeRepository extends JpaRepository<Like, Integer> {
    boolean existsByUserIdAndDiaryId(Integer userId, Integer diaryId);  // 특정 사용자가 일기에 좋아요를 눌렀는지 여부..
    void deleteByUserIdAndDiaryId(Integer userId, Integer diaryId);     // 특정 사용자가 일기에 눌렀던 좋아요를 삭제..
    int countByDiaryId(Integer diaryId);    // 특정 일기에 달린 좋아요 수를 계산..
}
