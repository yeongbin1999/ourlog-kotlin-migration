package com.back.ourlog.domain.content.repository

import com.back.ourlog.domain.content.entity.Content
import com.back.ourlog.domain.content.entity.ContentType
import org.springframework.data.jpa.repository.JpaRepository

interface ContentRepository : JpaRepository<Content, Int> {
    fun findByExternalIdAndType(externalId: String, type: ContentType): Content?
}
