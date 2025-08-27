package com.back.ourlog.global.security.oauth;

import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String providerId;
    private String name;
    private String email;
    private String provider;

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver("id", (Map<String, Object>) attributes.get("response"));
        } else if ("kakao".equals(registrationId)) {
            return ofKakao("id", attributes);
        }

        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                (String) attributes.get("sub"),  // 고유 ID 필드: "sub"
                (String) attributes.get("name"),
                (String) attributes.get("email"),
                "google"
        );
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                (String) attributes.get("id"),
                (String) attributes.get("name"),
                (String) attributes.get("email"),
                "naver"
        );
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                String.valueOf(attributes.get("id")),
                (String) profile.get("nickname"),
                (String) kakaoAccount.get("email"),
                "kakao"
        );
    }

    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String providerId, String name, String email, String provider) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.providerId = providerId;
        this.name = name;
        this.email = email;
        this.provider = provider;
    }
}
