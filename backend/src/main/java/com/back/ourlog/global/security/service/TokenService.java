package com.back.ourlog.global.security.service;

import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import com.back.ourlog.global.security.jwt.JwtProvider;
import com.back.ourlog.global.security.jwt.RefreshTokenRepository;
import com.back.ourlog.global.security.jwt.TokenDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    @Getter
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailsService customUserDetailsService;

    public TokenDto issueTokens(CustomUserDetails userDetails, String deviceId) {
        String accessToken = jwtProvider.createAccessToken(userDetails);
        String refreshToken = jwtProvider.createRefreshToken(userDetails);

        refreshTokenRepository.save(userDetails.getId().toString(), deviceId, refreshToken, jwtProvider.getRefreshTokenExpiration());

        return new TokenDto(accessToken, refreshToken, jwtProvider.getRefreshTokenExpiration());
    }

    public TokenDto issueTokens(Long userId, String deviceId) {
        // Assuming you can load CustomUserDetails from userId or create a minimal one for token generation
        CustomUserDetails userDetails = customUserDetailsService.loadUserById(userId.toString());
        String accessToken = jwtProvider.createAccessToken(userDetails);
        String refreshToken = jwtProvider.createRefreshToken(userDetails);

        refreshTokenRepository.save(userId.toString(), deviceId, refreshToken, jwtProvider.getRefreshTokenExpiration());

        return new TokenDto(accessToken, refreshToken, jwtProvider.getRefreshTokenExpiration());
    }

    public TokenDto reIssueTokens(String presentedRefreshToken, String deviceId) {
        if (!jwtProvider.validateToken(presentedRefreshToken)) {
            throw new CustomException(ErrorCode.AUTH_EXPIRED_TOKEN);
        }

        String userId = jwtProvider.getUserIdFromToken(presentedRefreshToken);

        CustomUserDetails userDetails = customUserDetailsService.loadUserById(userId);
        String newAccessToken = jwtProvider.createAccessToken(userDetails);
        String newRefreshToken = jwtProvider.createRefreshToken(userDetails);

        Long result = refreshTokenRepository.rotateRefreshToken(userId, deviceId, presentedRefreshToken, newRefreshToken, jwtProvider.getRefreshTokenExpiration());

        if (result == null || result != 1L) {
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN); // 토큰 재사용, 변조 등 실패
        }

        return new TokenDto(newAccessToken, newRefreshToken, jwtProvider.getRefreshTokenExpiration());
    }

    public void logout(Integer userId, String deviceId) {
        refreshTokenRepository.delete(userId.toString(), deviceId);
        // 필요하면 Access 토큰 블랙리스트 처리 추가 가능
    }

}
