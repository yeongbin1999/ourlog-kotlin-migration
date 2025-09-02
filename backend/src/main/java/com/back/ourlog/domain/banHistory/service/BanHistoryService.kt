package com.back.ourlog.domain.banHistory.service

import com.back.ourlog.domain.banHistory.dto.BanInfo
import com.back.ourlog.domain.banHistory.entity.BanHistory
import com.back.ourlog.domain.banHistory.entity.BanType
import com.back.ourlog.domain.banHistory.repository.BanHistoryRepository
import com.back.ourlog.domain.user.repository.UserRepository
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Service
class BanHistoryService(
    private val banHistoryRepository: BanHistoryRepository,
    private val userRepository: UserRepository,
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {

    fun banUser(userId: Int, reason: String, banDuration: Duration) {
        val user = userRepository.findById(userId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        val activeBan = banHistoryRepository.findActiveBanByUser(user)
        if (activeBan != null) {
            throw CustomException(ErrorCode.BAN_ALREADY_EXISTS)
        }

        val now = LocalDateTime.now()
        val expiredAt = now.plus(banDuration)

        val ban = BanHistory(
            user = user,
            banType = if (banDuration.isZero) BanType.PERMANENT else BanType.TEMPORARY,
            reason = reason,
            expiredAt = expiredAt
        )

        val saved = banHistoryRepository.save(ban)

        // Redis 캐시에 저장
        val banInfo = BanInfo.from(saved)
        val ttl = Duration.between(now, expiredAt)
        redisTemplate.opsForValue().set(getBanKey(userId), serialize(banInfo), ttl)
    }

    fun isUserBanned(userId: Int): Boolean {
        val key = getBanKey(userId)

        // Redis 캐시 우선 확인
        redisTemplate.opsForValue().get(key)?.let { cached ->
            return deserialize(cached).isStillBanned
        }

        // 캐시에 없으면 DB 조회 후 캐시 업데이트
        val user = userRepository.findById(userId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        val activeBan = banHistoryRepository.findActiveBanByUser(user)
        if (activeBan != null) {
            val banInfo = BanInfo.from(activeBan)
            val ttl = Duration.between(LocalDateTime.now(), activeBan.expiredAt)
            redisTemplate.opsForValue().set(key, serialize(banInfo), ttl)
            return true
        }
        return false
    }

    fun unbanUser(userId: Int) {
        // DB에서 만료 처리 (옵션)
        // banHistoryRepository.updateExpired(userId, LocalDateTime.now())

        // Redis 캐시 제거
        redisTemplate.delete(getBanKey(userId))
    }

    private fun getBanKey(userId: Int): String = "$BAN_KEY_PREFIX$userId"

    private fun serialize(banInfo: BanInfo): String =
        try {
            objectMapper.writeValueAsString(banInfo)
        } catch (e: Exception) {
            throw RuntimeException("Failed to serialize BanInfo", e)
        }

    private fun deserialize(json: String): BanInfo =
        try {
            objectMapper.readValue(json, BanInfo::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Failed to deserialize BanInfo", e)
        }

    companion object {
        private const val BAN_KEY_PREFIX = "ban:user:"
    }
}