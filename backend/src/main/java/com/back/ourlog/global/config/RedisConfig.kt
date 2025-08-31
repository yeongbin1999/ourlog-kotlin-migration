package com.back.ourlog.global.config

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
        val valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)

        val config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
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