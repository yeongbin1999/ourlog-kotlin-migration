package com.back.ourlog.domain.tag.repository

import com.back.ourlog.domain.tag.entity.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository : JpaRepository<Tag, Int> {
    fun findByName(name: String): Tag?
}
