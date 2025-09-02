package com.back.ourlog.global.security.oauth

data class OAuthAttributes(
    val attributes: Map<String, Any>,
    val providerId: String,
    val name: String,
    val email: String,
    val provider: String
) {
    companion object {

        fun of(registrationId: String, attributes: Map<String, Any>): OAuthAttributes {
            return when (registrationId.lowercase()) {
                "naver" -> ofNaver(attributes["response"] as? Map<*, *>)
                "kakao" -> ofKakao(attributes)
                else -> ofGoogle(attributes)
            }
        }

        private fun ofGoogle(attributes: Map<String, Any>): OAuthAttributes {
            val providerId = attributes["sub"]?.toString().orEmpty()
            val name = attributes["name"]?.toString().orEmpty()
            val email = attributes["email"]?.toString().orEmpty()
            val profileImageUrl = attributes["picture"]?.toString() // Google's profile picture URL

            val modifiedAttributes = attributes.toMutableMap()
            if (profileImageUrl != null) {
                modifiedAttributes["profileImageUrl"] = profileImageUrl
            }

            return OAuthAttributes(
                attributes = modifiedAttributes as Map<String, Any>,
                providerId = providerId,
                name = name,
                email = email,
                provider = "google"
            )
        }

        private fun ofNaver(response: Map<*, *>?): OAuthAttributes {
            val resp = response ?: emptyMap<Any, Any>()
            val providerId = resp["id"]?.toString().orEmpty()
            val name = resp["name"]?.toString().orEmpty()
            val email = resp["email"]?.toString().orEmpty()
            val profileImageUrl = resp["profile_image"]?.toString() // Naver's profile image URL

            val modifiedAttributes = resp.mapKeys { it.key.toString() }.toMutableMap()
            if (profileImageUrl != null) {
                modifiedAttributes["profileImageUrl"] = profileImageUrl
            }

            return OAuthAttributes(
                attributes = modifiedAttributes as Map<String, Any>,
                providerId = providerId,
                name = name,
                email = email,
                provider = "naver"
            )
        }

        private fun ofKakao(attributes: Map<String, Any>): OAuthAttributes {
            val kakaoAccount = attributes["kakao_account"] as? Map<*, *> ?: emptyMap<Any, Any>()
            val profile = kakaoAccount["profile"] as? Map<*, *> ?: emptyMap<Any, Any>()

            val providerId = attributes["id"]?.toString().orEmpty()
            val name = profile["nickname"]?.toString().orEmpty()
            val email = kakaoAccount["email"]?.toString() ?: "$providerId@kakao.com"
            val profileImageUrl = profile["profile_image_url"]?.toString() // Kakao's profile image URL

            val modifiedAttributes = attributes.toMutableMap()
            if (profileImageUrl != null) {
                modifiedAttributes["profileImageUrl"] = profileImageUrl
            }

            return OAuthAttributes(
                attributes = modifiedAttributes as Map<String, Any>,
                providerId = providerId,
                name = name,
                email = email,
                provider = "kakao"
            )
        }
    }
}