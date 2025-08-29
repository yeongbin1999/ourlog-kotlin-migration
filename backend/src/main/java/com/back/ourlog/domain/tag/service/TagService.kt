package com.back.ourlog.domain.tag.service

import com.back.ourlog.domain.tag.dto.TagResponse
import com.back.ourlog.domain.tag.entity.Tag
import com.back.ourlog.domain.tag.repository.TagRepository
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class TagService(
    private val tagRepository: TagRepository
) {

    fun getTagsByIds(ids: List<Int>): List<Tag> {
        val tags = tagRepository.findAllById(ids)
        if (tags.isEmpty()) throw CustomException(ErrorCode.TAG_NOT_FOUND)
        return tags
    }

    fun getAllTags(): List<TagResponse> {
        return tagRepository.findAll().map {
            TagResponse(it.id ?: -1, it.name)
        }
    }
}
