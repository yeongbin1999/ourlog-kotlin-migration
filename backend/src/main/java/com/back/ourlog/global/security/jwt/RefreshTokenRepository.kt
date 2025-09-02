package com.back.ourlog.global.security.jwt

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RefreshTokenRepository (
    private val redisTemplate: StringRedisTemplate,
    private val refreshTokenRotationScript: DefaultRedisScript<Long?>
) {
    private fun buildKey(userId: String, deviceId: String) = "refreshToken:$userId:$deviceId"

    fun save(userId: String, deviceId: String, refreshToken: String, expiration: Duration) {
        redisTemplate.opsForValue().set(buildKey(userId, deviceId), refreshToken, expiration)
    }

    fun find(userId: String, deviceId: String): String? {
        return redisTemplate.opsForValue().get(buildKey(userId, deviceId))
    }

    fun delete(userId: String, deviceId: String) {
        redisTemplate.delete(buildKey(userId, deviceId))
    }

    fun rotateRefreshToken(
        userId: String,
        deviceId: String,
        oldToken: String,
        newToken: String,
        expiration: Duration
    ): Long {
        val key = buildKey(userId, deviceId)
        // KEYS[1] = key, ARGV[1] = oldToken, ARGV[2] = newToken, ARGV[3] = expiration in seconds
        return redisTemplate.execute<Long>(
            refreshTokenRotationScript,
            mutableListOf(key),
            oldToken,
            newToken,
            expiration.seconds.toString()
        )
    }
}