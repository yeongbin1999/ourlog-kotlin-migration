package com.back.ourlog.domain.statistics.repository;

import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.statistics.dto.FavoriteEmotionAndCountDto;
import com.back.ourlog.domain.statistics.dto.FavoriteTypeAndCountDto;
import com.back.ourlog.domain.statistics.dto.MonthlyDiaryCount;
import com.back.ourlog.domain.statistics.dto.TypeCountDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatisticsRepository extends JpaRepository<Diary, Integer>,StatisticsRepositoryCustom {

    @Query("SELECT COUNT(d) FROM Diary d WHERE d.user.id = :userId")
    long getTotalDiaryCountByUserId(@Param("userId") int userId);

    // 평균 평점 조회, 없으면 0.0 반환
    @Query(value = "SELECT ROUND(AVG(rating), 2) FROM diary WHERE user_id = :userId", nativeQuery = true)
    Optional<Double> getAverageRatingByUserId(@Param("userId") int userId);

    @Query(value = """
        SELECT c.type AS favoriteContentType, COUNT(*) AS contentTypeCount
        FROM diary d
        JOIN content c ON d.content_id = c.id
        WHERE d.user_id = :userId
        GROUP BY c.type
        ORDER BY contentTypeCount DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<FavoriteTypeAndCountDto> findFavoriteTypeAndCountByUserId(@Param("userId") int userId);

    @Query(value = """
        SELECT t.name AS favoriteEmotion, COUNT(*) AS favoriteEmotionCount
        FROM diary_tag dt
        JOIN diary d ON dt.diary_id = d.id
        JOIN tag t ON dt.tag_id = t.id
        WHERE d.user_id = :userId
        GROUP BY t.name
        ORDER BY favoriteEmotionCount DESC
        LIMIT 1""", nativeQuery = true)
    Optional<FavoriteEmotionAndCountDto> findFavoriteEmotionAndCountByUserId(@Param("userId") int userId);

    @Query(value = "SELECT c.type as type, COUNT(d.id) as count " +
            "FROM diary d JOIN content c ON d.content_id = c.id " +
            "WHERE d.user_id = :userId " +
            "GROUP BY c.type " +
            "ORDER BY count DESC",
            nativeQuery = true)
    Optional<List<TypeCountDto>> findTypeCountsByUserId(@Param("userId") Integer userId);



}
