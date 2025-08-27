package com.back.ourlog.domain.banHistory.dto;

import com.back.ourlog.domain.banHistory.entity.BanHistory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BanInfo implements Serializable {
    private LocalDateTime bannedAt;
    private LocalDateTime expiredAt;
    private String reason;

    @JsonIgnore
    public boolean isStillBanned() {
        return expiredAt.isAfter(LocalDateTime.now());
    }

    public static BanInfo from(BanHistory ban) {
        return new BanInfo(ban.getBannedAt(), ban.getExpiredAt(), ban.getReason());
    }
}
