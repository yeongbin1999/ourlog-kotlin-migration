package com.back.ourlog.config

import com.back.ourlog.domain.user.entity.User
import com.back.ourlog.external.common.ContentSearchFacade
import com.back.ourlog.global.rq.Rq
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class ContentTestMockConfig {

    @Bean
    fun contentSearchFacade(): ContentSearchFacade =
        Mockito.mock(ContentSearchFacade::class.java)

    @Bean
    @Primary
    fun testRq(): Rq = Mockito.mock(Rq::class.java)

    @Bean
    fun testUserFactory(): (Int) -> User {
        return { id: Int ->
            val user = Mockito.mock(User::class.java)
            Mockito.`when`(user.id).thenReturn(id)
            user
        }
    }
}
