package com.back.ourlog.domain.auth.service;

import com.back.ourlog.domain.auth.dto.OAuthCallbackRequest;
import com.back.ourlog.global.security.jwt.TokenDto;
import com.back.ourlog.global.security.service.CustomUserDetails;
import com.back.ourlog.global.security.service.TokenService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final RestTemplate restTemplate;
    private final TokenService tokenService;
    private final AuthService authService;

    @Value("${oauth2.google.client-id}")
    private String googleClientId;
    @Value("${oauth2.google.client-secret}")
    private String googleClientSecret;
    @Value("${oauth2.google.token-uri}")
    private String googleTokenUri;
    @Value("${oauth2.google.user-info-uri}")
    private String googleUserInfoUri;

    @Value("${oauth2.kakao.client-id}")
    private String kakaoClientId;
    @Value("${oauth2.kakao.client-secret}")
    private String kakaoClientSecret;
    @Value("${oauth2.kakao.token-uri}")
    private String kakaoTokenUri;
    @Value("${oauth2.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    @Value("${oauth2.naver.client-id}")
    private String naverClientId;
    @Value("${oauth2.naver.client-secret}")
    private String naverClientSecret;
    @Value("${oauth2.naver.token-uri}")
    private String naverTokenUri;
    @Value("${oauth2.naver.user-info-uri}")
    private String naverUserInfoUri;

    public TokenDto handleOAuthCallback(String provider, OAuthCallbackRequest request, String deviceId) {
        String accessToken = null;
        String email = null;
        String name = null;
        String profileImageUrl = null;

        switch (provider.toLowerCase()) {
            case "google":
                accessToken = exchangeCodeForGoogleToken(request.code(), request.codeVerifier(), request.redirectUri());
                Map<String, String> googleUserInfo = getGoogleUserInfo(accessToken);
                email = googleUserInfo.get("email");
                name = googleUserInfo.get("name");
                profileImageUrl = googleUserInfo.get("profileImageUrl");
                break;
            case "kakao":
                accessToken = exchangeCodeForKakaoToken(request.code(), request.codeVerifier(), request.redirectUri());
                Map<String, String> kakaoUserInfo = getKakaoUserInfo(accessToken);
                email = kakaoUserInfo.get("email");
                name = kakaoUserInfo.get("nickname");
                profileImageUrl = kakaoUserInfo.get("profileImageUrl");
                break;
            case "naver":
                accessToken = exchangeCodeForNaverToken(request.code(), request.codeVerifier(), request.redirectUri());
                Map<String, String> naverUserInfo = getNaverUserInfo(accessToken);
                email = naverUserInfo.get("email");
                name = naverUserInfo.get("name");
                profileImageUrl = naverUserInfo.get("profileImageUrl");
                break;
            default:
                throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        }

        // Use the extracted information to find or create the user
        CustomUserDetails userDetails = authService.findOrCreateOAuthUser(email, name, profileImageUrl, provider);

        // Issue tokens using the userDetails
        TokenDto tokens = tokenService.issueTokens(userDetails, deviceId);

        return tokens;
    }

    private String exchangeCodeForGoogleToken(String code, String codeVerifier, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");
        params.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(googleTokenUri, request, JsonNode.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().get("access_token").asText();
        } else {
            // TODO: 에러 처리 로직 추가
            throw new RuntimeException("Failed to exchange code for Google token: " + response.getStatusCode());
        }
    }

    private Map<String, String> getGoogleUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(googleUserInfoUri, request, JsonNode.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("email", response.getBody().has("email") ? response.getBody().get("email").asText() : null);
            userInfo.put("name", response.getBody().has("name") ? response.getBody().get("name").asText() : null);
            userInfo.put("profileImageUrl", response.getBody().has("picture") ? response.getBody().get("picture").asText() : null); // Google uses 'picture' for profile image
            return userInfo;
        } else {
            // TODO: 에러 처리 로직 추가
            throw new RuntimeException("Failed to get Google user info: " + response.getStatusCode());
        }
    }

    private String exchangeCodeForKakaoToken(String code, String codeVerifier, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("client_secret", kakaoClientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        // Kakao does not directly use code_verifier in the token exchange for PKCE in the same way Google does.
        // PKCE for Kakao usually involves the 'state' parameter or is handled differently.
        // You might need to adjust this based on Kakao's specific PKCE implementation details.
        // For simplicity, I'm including it here, but verify Kakao's documentation.
        // params.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(kakaoTokenUri, request, JsonNode.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().get("access_token").asText();
        } else {
            // TODO: 에러 처리 로직 추가
            throw new RuntimeException("Failed to exchange code for Kakao token: " + response.getStatusCode());
        }
    }

    private Map<String, String> getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // Kakao user info often requires this

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(kakaoUserInfoUri, request, JsonNode.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, String> userInfo = new HashMap<>();
            JsonNode kakaoAccount = response.getBody().get("kakao_account");
            if (kakaoAccount != null) {
                userInfo.put("email", kakaoAccount.has("email") ? kakaoAccount.get("email").asText() : null);
                JsonNode profile = kakaoAccount.get("profile");
                if (profile != null) {
                    userInfo.put("nickname", profile.has("nickname") ? profile.get("nickname").asText() : null);
                    userInfo.put("profileImageUrl", profile.has("profile_image_url") ? profile.get("profile_image_url").asText() : null); // Kakao uses 'profile_image_url'
                }
            }
            return userInfo;
        } else {
            // TODO: 에러 처리 로직 추가
            throw new RuntimeException("Failed to get Kakao user info: " + response.getStatusCode());
        }
    }

    private String exchangeCodeForNaverToken(String code, String codeVerifier, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        // Naver does not directly use code_verifier in the token exchange for PKCE in the same way Google does.
        // Naver's PKCE implementation might be different or not explicitly required for this flow.
        // params.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(naverTokenUri, request, JsonNode.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().get("access_token").asText();
        } else {
            // TODO: 에러 처리 로직 추가
            throw new RuntimeException("Failed to exchange code for Naver token: " + response.getStatusCode());
        }
    }

    private Map<String, String> getNaverUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.add("X-Naver-Client-Id", naverClientId);
        headers.add("X-Naver-Client-Secret", naverClientSecret);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(naverUserInfoUri, request, JsonNode.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, String> userInfo = new HashMap<>();
            JsonNode responseNode = response.getBody().get("response"); // Naver wraps user info in a 'response' field
            if (responseNode != null) {
                userInfo.put("email", responseNode.has("email") ? responseNode.get("email").asText() : null);
                userInfo.put("name", responseNode.has("name") ? responseNode.get("name").asText() : null);
                userInfo.put("profileImageUrl", responseNode.has("profile_image") ? responseNode.get("profile_image").asText() : null); // Naver uses 'profile_image'
            }
            return userInfo;
        } else {
            // TODO: 에러 처리 로직 추가
            throw new RuntimeException("Failed to get Naver user info: " + response.getStatusCode());
        }
    }
}