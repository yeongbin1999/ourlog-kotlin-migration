package com.back.ourlog.global.config

import com.back.ourlog.domain.diary.dto.DiaryDetailDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@EnableCaching
@Configuration
class RedisConfig {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): StringRedisTemplate {
        return StringRedisTemplate(connectionFactory)
    }

    /**
     * Object 저장용 템플릿
     * 전역 ObjectMapper 설정을 그대로 사용해 GenericJackson2JsonRedisSerializer 구성
     * → @class 메타 포함으로 타입 복원 안정화
     */

    @Bean
    fun objectRedisTemplate(
        connectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper
    ): RedisTemplate<String, Any> {
        val keySerializer = StringRedisSerializer()
        val valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)

        return RedisTemplate<String, Any>().apply {
            this.connectionFactory = connectionFactory
            this.keySerializer = keySerializer
            this.valueSerializer = valueSerializer
            this.hashKeySerializer = keySerializer
            this.hashValueSerializer = valueSerializer
        }
    }

    /**
     * 캐시 매니저
     * - 키: String
     * - 값: GenericJackson2JsonRedisSerializer (ObjectMapper 공유)
     * - 기본 TTL: 1시간
     */

    @Bean
    fun redisCacheManager(
        factory: RedisConnectionFactory,
        objectMapper: ObjectMapper
    ): RedisCacheManager {
        val keySerializer = StringRedisSerializer()
        val genericValueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)

        // 기본 설정 (1시간 TTL, null 캐시 금지)
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericValueSerializer))
            .entryTtl(Duration.ofHours(1))
            .disableCachingNullValues()

        // 1) DiaryDetailDto는 타입 지정 직렬화기 -> 역직렬화 안정
        val diaryDetailSerializer =
            Jackson2JsonRedisSerializer(objectMapper, DiaryDetailDto::class.java)

        val diaryDetailConfig = defaultConfig
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(diaryDetailSerializer))
            .entryTtl(Duration.ofMinutes(10)) // 상세는 10분

        // 2) 라이브러리 검색/도서/외부콘텐츠는 generic + 30분 TTL
        val libraryConfig = defaultConfig.entryTtl(Duration.ofMinutes(30))

        val perCacheConfigs = mapOf(
            com.back.ourlog.global.config.cache.CacheNames.DIARY_DETAIL to diaryDetailConfig,
            com.back.ourlog.global.config.cache.CacheNames.LIBRARY_BOOKS to libraryConfig,
            com.back.ourlog.global.config.cache.CacheNames.LIBRARY_SEARCH_RESULTS to libraryConfig,
            com.back.ourlog.global.config.cache.CacheNames.EXTERNAL_CONTENT to libraryConfig
        )

        return RedisCacheManager.builder(factory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(perCacheConfigs)
            .transactionAware()
            .build()
    }

    /**
     * Refresh Token 회전용 Lua 스크립트
     */

    @Bean("refreshTokenRotationScript")
    fun refreshTokenRotationScript(): DefaultRedisScript<Long> {
        return DefaultRedisScript<Long>().apply {

            setLocation(ClassPathResource("redis/refresh_token_rotate.lua"))
            setResultType(Long::class.java)
        }
    }
}