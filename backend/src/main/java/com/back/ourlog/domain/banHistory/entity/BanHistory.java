package com.back.ourlog.domain.banHistory.entity;

import com.back.ourlog.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class BanHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = LAZY, optional = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BanType banType;

    @Column(length = 255)
    private String reason;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime bannedAt;

    private LocalDateTime expiredAt;

    public boolean isActiveNow() {
        return banType == BanType.PERMANENT || (expiredAt != null && expiredAt.isAfter(LocalDateTime.now()));
    }
}