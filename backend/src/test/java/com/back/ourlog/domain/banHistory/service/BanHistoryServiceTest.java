package com.back.ourlog.domain.banHistory.service;

import com.back.ourlog.domain.banHistory.dto.BanInfo;
import com.back.ourlog.domain.banHistory.entity.BanHistory;
import com.back.ourlog.domain.banHistory.repository.BanHistoryRepository;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.repository.UserRepository;
import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BanHistoryServiceTest {

    @Mock private BanHistoryRepository banHistoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    private ObjectMapper objectMapper;
    private BanHistoryService banHistoryService;

    private final Integer userId = 1;
    private final User user = User.builder().id(userId).build();

    @BeforeEach
    void setUp() {
        // Redis stub 제거함
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        banHistoryService = new BanHistoryService(
                banHistoryRepository,
                userRepository,
                redisTemplate,
                objectMapper
        );
    }

    @Test
    @DisplayName("밴 처리 성공 및 캐시 저장 테스트")
    void banUser_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Duration duration = Duration.ofHours(2);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plus(duration);

        BanHistory banHistory = BanHistory.builder()
                .user(user)
                .reason("Abusive Language")
                .bannedAt(now)
                .expiredAt(expiredAt)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(banHistoryRepository.findActiveBanByUser(user)).thenReturn(Optional.empty());
        when(banHistoryRepository.save(any())).thenReturn(banHistory);

        banHistoryService.banUser(userId, "Abusive Language", duration);

        verify(banHistoryRepository).save(any(BanHistory.class));
        verify(valueOperations).set(startsWith("ban:user:"), anyString(), eq(duration));
    }

    @Test
    @DisplayName("이미 밴된 사용자인 경우 예외 발생 테스트")
    void banUser_ThrowsException_WhenUserAlreadyBanned() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(banHistoryRepository.findActiveBanByUser(user)).thenReturn(Optional.of(mock(BanHistory.class)));

        assertThatThrownBy(() -> banHistoryService.banUser(userId, "Abusive Language", Duration.ofHours(1)))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.BAN_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("캐시에 밴 정보가 존재하면 true 반환 테스트")
    void isUserBanned_ReturnsTrue_WhenCacheExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        BanInfo mockBanInfo = new BanInfo(LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Abusive Language");
        String serialized = toJson(mockBanInfo);
        when(valueOperations.get(anyString())).thenReturn(serialized);

        boolean result = banHistoryService.isUserBanned(userId);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("캐시에 없고 DB에 존재하면 true 반환 및 캐시 저장 테스트")
    void isUserBanned_ReturnsTrue_WhenNotInCacheButExistsInDb() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        BanHistory banHistory = BanHistory.builder()
                .user(user)
                .reason("Abusive Language")
                .bannedAt(LocalDateTime.now().minusMinutes(5))
                .expiredAt(LocalDateTime.now().plusMinutes(30))
                .build();

        when(valueOperations.get(anyString())).thenReturn(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(banHistoryRepository.findActiveBanByUser(user)).thenReturn(Optional.of(banHistory));

        boolean result = banHistoryService.isUserBanned(userId);

        assertThat(result).isTrue();
        verify(valueOperations).set(startsWith("ban:user:"), anyString(), any());
    }

    @Test
    @DisplayName("캐시와 DB 모두에 밴 정보가 없으면 false 반환 테스트")
    void isUserBanned_ReturnsFalse_WhenNotInCacheAndNotInDb() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get(anyString())).thenReturn(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(banHistoryRepository.findActiveBanByUser(user)).thenReturn(Optional.empty());

        boolean result = banHistoryService.isUserBanned(userId);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("밴 해제 시 Redis 캐시 삭제 호출 테스트")
    void unbanUser_DeletesCache() {
        banHistoryService.unbanUser(userId);

        verify(redisTemplate).delete("ban:user:" + userId);
    }

    private String toJson(BanInfo banInfo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.writeValueAsString(banInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}