package com.back.ourlog.domain.timeline.repository;

import com.back.ourlog.domain.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TimelineRepository extends JpaRepository<Diary, Integer> {

    @Query("SELECT d FROM Diary d WHERE d.isPublic = true ORDER BY d.createdAt DESC")
    List<Diary> findPublicDiaries();    // 공개된 일기만 최신순 가져오기..
}