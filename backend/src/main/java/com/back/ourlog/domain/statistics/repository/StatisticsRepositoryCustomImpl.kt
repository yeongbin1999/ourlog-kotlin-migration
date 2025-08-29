package com.back.ourlog.domain.statistics.repository;

import com.back.ourlog.domain.content.entity.ContentType;

import com.back.ourlog.domain.statistics.dto.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StatisticsRepositoryCustomImpl implements  StatisticsRepositoryCustom{

    private final EntityManager em;

    @Value("${app.sql.lineMonthly}")
    private String lineMonthlySql;

    @Value("${app.sql.lineDaily}")
    private String lineDailySql;

    @Override
    public List<MonthlyDiaryCount> countMonthlyDiaryByUserId(Integer userId, LocalDateTime startDate) {
        String sql =
                "SELECT " + lineMonthlySql + " AS period, COUNT(*) AS views " +
                        "FROM diary d " +
                        "WHERE user_id = ? AND created_at >= ? " +
                        "GROUP BY  "+ lineMonthlySql +
                        " ORDER BY "+ lineMonthlySql +" ASC";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter(1, userId)
                .setParameter(2, startDate)
                .getResultList();

        return rows.stream()
                .map(row -> new MonthlyDiaryCount(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .toList();
    }

    @Override
    public List<TypeLineGraphDto> findTypeLineMonthly(Integer userId, LocalDateTime start, LocalDateTime end) {
        String sql =
                "SELECT " + lineMonthlySql + " AS axisLabel, " +
                "  c.type AS type,"+
                "  COUNT(*) AS count " +
                "FROM diary d " +
                "JOIN content c ON d.content_id = c.id " +
                "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                "GROUP BY axisLabel, c.type " +
                "ORDER BY axisLabel, c.type";
        List<Object[]> result = em.createNativeQuery(sql)
                .setParameter(1, userId)
                .setParameter(2, start)
                .setParameter(3, end)
                .getResultList();

        return result.stream()
                .map(row -> new TypeLineGraphDto(
                        (String) row[0],
                        ContentType.valueOf((String) row[1]),
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }

    @Override
    public List<TypeLineGraphDto> findTypeLineDaily(Integer userId, LocalDateTime start, LocalDateTime end) {
        String sql =
                "SELECT " + lineDailySql + " AS axisLabel, " +
                "  c.type AS type, " +
                "  COUNT(*) AS count " +
                "FROM diary d " +
                "JOIN content c ON d.content_id = c.id " +
                "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                "GROUP BY axisLabel, c.type " +
                "ORDER BY axisLabel, c.type";
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter(1, userId)
                .setParameter(2, start)
                .setParameter(3, end)
                .getResultList();

        return rows.stream()
                .map(row -> new TypeLineGraphDto(
                        (String)row[0],
                        ContentType.valueOf((String)row[1]),
                        ((Number)row[2]).longValue()
                ))
                .toList();
    }

    @Override
    public List<TypeRankDto> findTypeRanking(Integer userId, LocalDateTime start, LocalDateTime end) {
        String jpql =
                "select new com.back.ourlog.domain.statistics.dto.TypeRankDto(" +
                        "  c.type, count(d)" +
                        ") " +
                        "from Diary d join d.content c " +
                        "where d.user.id = :uid and d.createdAt between :s and :e " +
                        "group by c.type order by count(d) desc";
        return em.createQuery(jpql, TypeRankDto.class)
                .setParameter("uid", userId)
                .setParameter("s",   start)
                .setParameter("e",   end)
                .getResultList();
    }


    @Override
    public List<GenreLineGraphDto> findGenreLineMonthly(Integer userId, LocalDateTime start, LocalDateTime end) {
        String sql =
                "SELECT " + lineMonthlySql + " AS axisLabel, " +
                        "       g.name AS genre, " +
                        "       COUNT(*) AS cnt " +
                        "FROM diary d " +
                        "JOIN diary_genre dg ON d.id = dg.diary_id " +
                        "JOIN genre g ON dg.genre_id = g.id " +
                        "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                        "GROUP BY axisLabel, g.name " +
                        "ORDER BY axisLabel, g.name";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter(1, userId)
                .setParameter(2, start)
                .setParameter(3, end)
                .getResultList();

        return rows.stream()
                .map(r -> new GenreLineGraphDto(
                        (String) r[0],
                        (String) r[1],
                        ((Number) r[2]).longValue()
                ))
                .toList();
    }

    @Override
    public List<GenreLineGraphDto> findGenreLineDaily(Integer userId, LocalDateTime start, LocalDateTime end) {
        String sql =
                "SELECT " + lineDailySql + " AS axisLabel, " +
                        "       g.name AS genre, " +
                        "       COUNT(*) AS cnt " +
                        "FROM diary d " +
                        "JOIN diary_genre dg ON d.id = dg.diary_id " +
                        "JOIN genre g ON dg.genre_id = g.id " +
                        "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                        "GROUP BY axisLabel, g.name " +
                        "ORDER BY axisLabel, g.name";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter(1, userId)
                .setParameter(2, start)
                .setParameter(3, end)
                .getResultList();

        return rows.stream()
                .map(r -> new GenreLineGraphDto(
                        (String) r[0],
                        (String) r[1],
                        ((Number) r[2]).longValue()
                ))
                .toList();
    }

    @Override
    public List<GenreRankDto> findGenreRanking(Integer userId, LocalDateTime start, LocalDateTime end) {
        String sql =
                "SELECT g.name AS genre, COUNT(*) AS totalCount " +
                        "FROM diary d " +
                        "JOIN diary_genre dg ON d.id = dg.diary_id " +
                        "JOIN genre g ON dg.genre_id = g.id " +
                        "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                        "GROUP BY g.name ORDER BY totalCount DESC";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter(1, userId)
                .setParameter(2, start)
                .setParameter(3, end)
                .getResultList();

        return rows.stream()
                .map(r -> new GenreRankDto(
                        (String) r[0],
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }


    @Override
    public List<EmotionLineGraphDto> findEmotionLineMonthly(Integer userId, LocalDateTime start, LocalDateTime end) {
        String sql =
                "SELECT "+ lineMonthlySql + " AS axisLabel, " +
                        "       t.name AS emotion, " +
                        "       COUNT(*) AS cnt " +
                        "FROM diary d " +
                        "JOIN diary_tag dt ON d.id = dt.diary_id " +
                        "JOIN tag t ON dt.tag_id = t.id " +
                        "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                        "GROUP BY axisLabel, t.name " +
                        "ORDER BY axisLabel, t.name";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter(1, userId)
                .setParameter(2, start)
                .setParameter(3, end)
                .getResultList();

        return rows.stream()
                .map(r -> new EmotionLineGraphDto(
                        (String) r[0],
                        (String) r[1],
                        ((Number) r[2]).longValue()
                ))
                .toList();
    }

    @Override
    public List<EmotionLineGraphDto> findEmotionLineDaily(Integer userId, LocalDateTime start, LocalDateTime end) {
        String sql =
                "SELECT " + lineDailySql + " AS axisLabel, " +
                        "       t.name AS emotion, " +
                        "       COUNT(*) AS cnt " +
                        "FROM diary d " +
                        "JOIN diary_tag dt ON d.id = dt.diary_id " +
                        "JOIN tag t ON dt.tag_id = t.id " +
                        "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                        "GROUP BY axisLabel, t.name " +
                        "ORDER BY axisLabel, t.name";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter(1, userId)
                .setParameter(2, start)
                .setParameter(3, end)
                .getResultList();

        return rows.stream()
                .map(r -> new EmotionLineGraphDto(
                        (String) r[0],
                        (String) r[1],
                        ((Number) r[2]).longValue()
                ))
                .toList();
    }

    @Override
    public List<EmotionRankDto> findEmotionRanking(Integer userId, LocalDateTime start, LocalDateTime end) {
        String sql =
                "SELECT t.name AS emotion, COUNT(*) AS totalCount " +
                        "FROM diary d " +
                        "JOIN diary_tag dt ON d.id = dt.diary_id " +
                        "JOIN tag t ON dt.tag_id = t.id " +
                        "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                        "GROUP BY t.name ORDER BY totalCount DESC";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter(1, userId)
                .setParameter(2, start)
                .setParameter(3, end)
                .getResultList();

        return rows.stream()
                .map(r -> new EmotionRankDto(
                        (String) r[0],
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    @Override
    public List<OttLineGraphDto> findOttLineMonthly(Integer userId, LocalDateTime start, LocalDateTime end) {
        String sql =
                "SELECT " + lineMonthlySql + " AS axisLabel, o.name AS ottName, COUNT(*) AS cnt " +
                        "FROM diary d " +
                        "JOIN diary_ott do ON d.id = do.diary_id " +
                        "JOIN ott o ON do.ott_id = o.id " +
                        "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                        "GROUP BY axisLabel, o.name ORDER BY axisLabel, o.name";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter(1, userId)
                .setParameter(2, start)
                .setParameter(3, end)
                .getResultList();

        return rows.stream()
                .map(r -> new OttLineGraphDto(
                        (String) r[0],
                        (String) r[1],
                        ((Number) r[2]).longValue()
                ))
                .toList();
    }

    @Override
    public List<OttLineGraphDto> findOttLineDaily(Integer userId, LocalDateTime start, LocalDateTime end) {
        String sql =
                "SELECT " + lineDailySql + " AS axisLabel, o.name AS ottName, COUNT(*) AS cnt " +
                        "FROM diary d " +
                        "JOIN diary_ott do ON d.id = do.diary_id " +
                        "JOIN ott o ON do.ott_id = o.id " +
                        "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                        "GROUP BY axisLabel, o.name ORDER BY axisLabel, o.name";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter(1, userId)
                .setParameter(2, start)
                .setParameter(3, end)
                .getResultList();

        return rows.stream()
                .map(r -> new OttLineGraphDto(
                        (String) r[0],
                        (String) r[1],
                        ((Number) r[2]).longValue()
                ))
                .toList();
    }

    @Override
    public List<OttRankDto> findOttRanking(Integer userId, LocalDateTime start, LocalDateTime end) {
        String sql =
                "SELECT o.name AS ottName, COUNT(*) AS totalCnt " +
                        "FROM diary d " +
                        "JOIN diary_ott do ON d.id = do.diary_id " +
                        "JOIN ott o ON do.ott_id = o.id " +
                        "WHERE d.user_id = ? AND d.created_at BETWEEN ? AND ? " +
                        "GROUP BY o.name ORDER BY totalCnt DESC";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter(1, userId)
                .setParameter(2, start)
                .setParameter(3, end)
                .getResultList();

        return rows.stream()
                .map(r -> new OttRankDto(
                        (String) r[0],
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }
}