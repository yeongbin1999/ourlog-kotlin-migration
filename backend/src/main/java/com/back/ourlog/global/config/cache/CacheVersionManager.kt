package com.back.ourlog.global.config.cache

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.cache.CacheManager
import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class CacheVersionManager(
    private val cacheManager: CacheManager,
    private val redisTemplate: StringRedisTemplate
) {

    private val logger = LoggerFactory.getLogger(CacheVersionManager::class.java)

    companion object {
        const val CACHE_VERSION = "v2.0" // 스키마 변경 시 버전업
        private const val CACHE_VERSION_KEY = "app:cache:version"

        // 캐시 이름 정의
        private val MANAGED_CACHE_NAMES = setOf(
            CacheNames.DIARY_DETAIL,
            CacheNames.LIBRARY_BOOKS,
            CacheNames.LIBRARY_SEARCH_RESULTS,
            CacheNames.EXTERNAL_CONTENT
        )
    }

    @EventListener(ApplicationReadyEvent::class)
    fun validateCacheVersion(event: ApplicationReadyEvent) {
        try {
            val currentVersion = redisTemplate.opsForValue().get(CACHE_VERSION_KEY)

            when {
                currentVersion == null -> {
                    logger.info("캐시 버전 정보가 없습니다. 새로운 버전으로 초기화: $CACHE_VERSION")
                    initializeCacheVersion()
                }
                currentVersion != CACHE_VERSION -> {
                    logger.warn("캐시 버전 불일치 감지. 현재: $currentVersion, 필요: $CACHE_VERSION")
                    migrateCacheVersion(currentVersion)
                }
                else -> {
                    logger.info("캐시 버전 일치: $CACHE_VERSION")
                }
            }
        } catch (e: Exception) {
            logger.error("캐시 버전 검증 중 오류 발생", e)
            // 오류 발생 시 안전하게 캐시 초기화
            clearAllManagedCaches()
            initializeCacheVersion()
        }
    }

    /**
     * 새로운 캐시 버전으로 초기화
     */
    private fun initializeCacheVersion() {
        redisTemplate.opsForValue().set(CACHE_VERSION_KEY, CACHE_VERSION)
        logger.info("캐시 버전 초기화 완료: $CACHE_VERSION")
    }

    /**
     * 캐시 버전 마이그레이션
     */
    private fun migrateCacheVersion(oldVersion: String) {
        logger.info("캐시 마이그레이션 시작: $oldVersion -> $CACHE_VERSION")

        when (oldVersion) {
            "v1.0" -> migrateFromV1ToV2()
            else -> {
                logger.warn("알 수 없는 버전입니다. 전체 캐시를 초기화합니다: $oldVersion")
                clearAllManagedCaches()
            }
        }

        // 버전 업데이트
        redisTemplate.opsForValue().set(CACHE_VERSION_KEY, CACHE_VERSION)
        logger.info("캐시 마이그레이션 완료: $CACHE_VERSION")
    }

    /**
     * v1.0에서 v2.0으로 마이그레이션
     * 기존 캐시 데이터가 새로운 직렬화 형식과 호환되지 않으므로 초기화
     */
    private fun migrateFromV1ToV2() {
        logger.info("v1.0 -> v2.0 마이그레이션: 직렬화 형식 변경으로 인한 캐시 초기화")
        clearAllManagedCaches()
    }

    /**
     * 관리되는 모든 캐시 초기화
     */
    private fun clearAllManagedCaches() {
        MANAGED_CACHE_NAMES.forEach { cacheName ->
            try {
                cacheManager.getCache(cacheName)?.clear()
                logger.info("캐시 초기화 완료: $cacheName")
            } catch (e: Exception) {
                logger.warn("캐시 초기화 실패: $cacheName", e)
            }
        }
    }

    /**
     * 특정 캐시 강제 초기화 (관리자 기능)
     */
    fun forceClearCache(cacheName: String): Boolean = try {
        cacheManager.getCache(cacheName)?.clear()
        logger.info("관리자 요청으로 캐시 초기화: $cacheName")
        true
    } catch (e: Exception) {
        logger.error("캐시 초기화 실패: $cacheName", e)
        false
    }

    /**
     * 현재 캐시 버전 조회
     */
    fun getCurrentCacheVersion(): String? =
        redisTemplate.opsForValue().get(CACHE_VERSION_KEY)

    /**
     * 캐시 상태 정보 조회
     */
    fun getCacheStatus(): Map<String, Any> = mapOf(
        "version" to (getCurrentCacheVersion() ?: "unknown"),
        "expectedVersion" to CACHE_VERSION,
        "managedCaches" to MANAGED_CACHE_NAMES.associateWith { cacheName ->
            try {
                val cache = cacheManager.getCache(cacheName)
                mapOf(
                    "exists" to (cache != null),
                    "native" to (cache?.nativeCache?.javaClass?.simpleName ?: "unknown")
                )
            } catch (e: Exception) {
                mapOf("error" to e.message)
            }
        }
    )
}