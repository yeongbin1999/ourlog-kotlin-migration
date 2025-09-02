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

            return OAuthAttributes(
                attributes = attributes,
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

            return OAuthAttributes(
                attributes = resp.mapKeys { it.key.toString() }.mapValues { it.value.toString() },
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


            return OAuthAttributes(
                attributes = attributes,
                providerId = providerId,
                name = name,
                email = email,
                provider = "kakao"
            )
        }
    }
}