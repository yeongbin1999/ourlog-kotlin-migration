package com.back.ourlog.domain.banHistory.service;

import com.back.ourlog.domain.banHistory.dto.BanInfo;
import com.back.ourlog.domain.banHistory.entity.BanHistory;
import com.back.ourlog.domain.banHistory.repository.BanHistoryRepository;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.repository.UserRepository;
import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BanHistoryService {

    private final BanHistoryRepository banHistoryRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private static final String BAN_KEY_PREFIX = "ban:user:";

    public void banUser(Integer userId, String reason, Duration banDuration) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (banHistoryRepository.findActiveBanByUser(user).isPresent()) {
            throw new CustomException(ErrorCode.BAN_ALREADY_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plus(banDuration);

        BanHistory ban = BanHistory.builder()
                .user(user)
                .reason(reason)
                .bannedAt(now)
                .expiredAt(expiredAt)
                .build();

        BanHistory saved = banHistoryRepository.save(ban);

        // Redis 캐시에 저장
        BanInfo banInfo = BanInfo.from(saved);
        Duration ttl = Duration.between(now, expiredAt);
        redisTemplate.opsForValue().set(getBanKey(userId), serialize(banInfo), ttl);
    }

    public boolean isUserBanned(Integer userId) {
        String key = getBanKey(userId);
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        String cached = ops.get(key);
        if (cached != null) {
            BanInfo banInfo = deserialize(cached);
            return banInfo.isStillBanned();
        }

        // Redis에 없으면 DB 조회 후 캐시
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Optional<BanHistory> optionalBan = banHistoryRepository.findActiveBanByUser(user);
        if (optionalBan.isPresent()) {
            BanHistory ban = optionalBan.get();
            BanInfo banInfo = BanInfo.from(ban);
            Duration ttl = Duration.between(LocalDateTime.now(), ban.getExpiredAt());
            redisTemplate.opsForValue().set(key, serialize(banInfo), ttl);
            return true;
        }

        return false;
    }

    public void unbanUser(Integer userId) {
        // 밴 해제 처리 로직 추가 가능
        redisTemplate.delete(getBanKey(userId));
    }

    private String getBanKey(Integer userId) {
        return BAN_KEY_PREFIX + userId;
    }

    private String serialize(BanInfo banInfo) {
        try {
            return objectMapper.writeValueAsString(banInfo);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize BanInfo", e);
        }
    }

    private BanInfo deserialize(String json) {
        try {
            return objectMapper.readValue(json, BanInfo.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize BanInfo", e);
        }
    }
}