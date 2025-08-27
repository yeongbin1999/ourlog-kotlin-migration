package com.back.ourlog.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> refreshTokenRotationScript;

    private String buildKey(String userId, String deviceId) {
        return "refreshToken:" + userId + ":" + deviceId;
    }

    public void save(String userId, String deviceId, String refreshToken, Duration expiration) {
        redisTemplate.opsForValue().set(buildKey(userId, deviceId), refreshToken, expiration);
    }

    public String find(String userId, String deviceId) {
        return redisTemplate.opsForValue().get(buildKey(userId, deviceId));
    }

    public void delete(String userId, String deviceId) {
        redisTemplate.delete(buildKey(userId, deviceId));
    }

    public Long rotateRefreshToken(String userId, String deviceId, String oldToken, String newToken, Duration expiration) {
        String key = buildKey(userId, deviceId);
        // KEYS[1] = key, ARGV[1] = oldToken, ARGV[2] = newToken, ARGV[3] = expiration in seconds
        return redisTemplate.execute(
                refreshTokenRotationScript,
                Collections.singletonList(key),
                oldToken,
                newToken,
                String.valueOf(expiration.getSeconds())
        );
    }
}