package com.back.ourlog.domain.banHistory.repository;

import com.back.ourlog.domain.banHistory.entity.BanHistory;
import com.back.ourlog.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BanHistoryRepository extends JpaRepository<BanHistory, Integer> {
    Optional<BanHistory> findTopByUserOrderByBannedAtDesc(User user);

    @Query("SELECT b FROM BanHistory b WHERE b.user = :user AND (b.expiredAt IS NULL OR b.expiredAt > CURRENT_TIMESTAMP)")
    Optional<BanHistory> findActiveBanByUser(@Param("user") User user);
}

