package com.back.ourlog.domain.auth.controller;

import com.back.ourlog.domain.auth.dto.LoginRequest;
import com.back.ourlog.domain.auth.dto.LoginResponse;
import com.back.ourlog.domain.auth.dto.OAuthCallbackRequest;
import com.back.ourlog.domain.auth.dto.SignupRequest;
import com.back.ourlog.domain.auth.service.AuthService;
import com.back.ourlog.domain.auth.service.OAuthService;
import com.back.ourlog.global.common.dto.RsData;
import com.back.ourlog.global.security.jwt.TokenDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuthService oauthService;

    @Value("${cookie.secure}")
    private boolean secure;

    @PostMapping("/signup")
    public RsData<?> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return RsData.success("회원가입 성공");
    }

    @PostMapping("/login")
    public RsData<LoginResponse> login(@RequestBody LoginRequest request,
                                       @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
                                       HttpServletResponse response) {

        var loginResult = authService.login(request.email(), request.password(), deviceId);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginResult.refreshToken())
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(loginResult.refreshTokenExpiration())
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return RsData.success("로그인 성공", new LoginResponse(loginResult.accessToken()));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public RsData<?> logout(@RequestHeader("Authorization") String authorization,
                            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
                            HttpServletResponse response) {

        String accessToken = authorization.replace("Bearer ", "");

        authService.logout(accessToken, deviceId);

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .maxAge(0)
                .secure(secure)
                .httpOnly(true)
                .build();

        response.addHeader("Set-Cookie", deleteCookie.toString());

        return RsData.success("로그아웃 성공");
    }

    @PostMapping("/reissue")
    public RsData<LoginResponse> reissue(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            HttpServletResponse response) {

        var newTokens = authService.reissue(refreshToken, deviceId);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", newTokens.refreshToken())
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(newTokens.refreshTokenExpiration())
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return RsData.success("토큰 재발급 성공", new LoginResponse(newTokens.accessToken()));
    }

    @PostMapping("/oauth/callback/{provider}")
    public RsData<LoginResponse> oauthCallback(@PathVariable String provider,
                                               @RequestBody OAuthCallbackRequest request,
                                               @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
                                               HttpServletResponse response) {

        TokenDto tokenDto = oauthService.handleOAuthCallback(provider, request, deviceId);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenDto.refreshToken())
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(tokenDto.refreshTokenExpiration())
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return RsData.success("OAuth 로그인 성공", new LoginResponse(tokenDto.accessToken()));
    }
}