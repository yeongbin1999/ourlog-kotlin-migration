package com.back.ourlog.domain.diary.repository;

import com.back.ourlog.domain.diary.entity.Diary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Integer> {
    Optional<Diary> findTopByOrderByIdDesc(); // 가장 최근에 작성된 일기 조회
    Page<Diary> findByUserId(Integer userId, Pageable pageable);
}
