package com.back.ourlog.config

import com.back.ourlog.external.common.ContentSearchFacade
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class ContentTestMockConfig {

    @Bean
    fun contentSearchFacade(): ContentSearchFacade =
        Mockito.mock(ContentSearchFacade::class.java)
}
