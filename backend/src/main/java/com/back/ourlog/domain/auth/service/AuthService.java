package com.back.ourlog.domain.auth.service;

import com.back.ourlog.domain.auth.dto.SignupRequest;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.repository.UserRepository;
import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import com.back.ourlog.global.security.jwt.TokenDto;
import com.back.ourlog.global.security.service.CustomUserDetails;
import com.back.ourlog.global.security.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new CustomException(ErrorCode.USER_DUPLICATE_EMAIL);
        }

        User user = User.createNormalUser(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname(),
                request.profileImageUrl(),
                request.bio());

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public TokenDto login(String email, String password, String deviceId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        return tokenService.issueTokens(userDetails, deviceId);
    }

    public void logout(String accessToken, String deviceId) {
        tokenService.logout(extractUserId(accessToken), deviceId);
    }

    public TokenDto reissue(String refreshToken, String deviceId) {
        return tokenService.reIssueTokens(refreshToken, deviceId);
    }

    private Integer extractUserId(String accessToken) {
        return Integer.parseInt(tokenService.getJwtProvider().getUserIdFromToken(accessToken));
    }

        @Transactional
    public CustomUserDetails findOrCreateOAuthUser(String providerId, String nickname, String profileImageUrl, String provider) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .map(CustomUserDetails::new)
                .orElseGet(() -> {
                    User newUser = User.createSocialUser(
                            provider,
                            providerId,
                            null, // email은 없을 수 있으므로 null
                            nickname,
                            profileImageUrl
                    );
                    return new CustomUserDetails(userRepository.save(newUser));
                });
    }
}
