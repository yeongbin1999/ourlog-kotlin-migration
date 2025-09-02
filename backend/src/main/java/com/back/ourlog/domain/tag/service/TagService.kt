package com.back.ourlog.domain.tag.service

import com.back.ourlog.domain.diary.entity.Diary
import com.back.ourlog.domain.tag.dto.TagResponse
import com.back.ourlog.domain.tag.entity.DiaryTag
import com.back.ourlog.domain.tag.entity.Tag
import com.back.ourlog.domain.tag.repository.TagRepository
import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TagService(
    private val tagRepository: TagRepository
) {
    fun getTagsByIds(ids: List<Int>): List<Tag> {
        val tags = tagRepository.findAllById(ids)
        if (tags.isEmpty()) throw CustomException(ErrorCode.TAG_NOT_FOUND)
        return tags
    }

    fun getAllTags(): List<TagResponse> =
        tagRepository.findAll().map { TagResponse(it.id ?: -1, it.name) }

    // 다이어리-태그 동기화
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    fun syncDiaryTags(diary: Diary, requestedNames: List<String>) {
        // 정규화
        val names = requestedNames.map { it.trim() }.filter { it.isNotBlank() }.distinct()

        val current = diary.diaryTags.map { it.tag.name }.toSet()
        val toAdd    = names.filter { it !in current }
        val toRemove = current.filter { it !in names }

        if (toAdd.isEmpty() && toRemove.isEmpty()) return // 변경 없음

        // 제거
        val it = diary.diaryTags.iterator()
        while (it.hasNext()) {
            val rel = it.next()
            if (rel.tag.name in toRemove) it.remove()
        }

        // 추가
        toAdd.forEach { name ->
            val tag = findOrCreateByNameSafe(name)
            diary.diaryTags.add(DiaryTag(diary, tag))
        }
    }

    private fun findOrCreateByNameSafe(name: String): Tag {
        tagRepository.findByName(name)?.let { return it }
        return try {
            tagRepository.save(Tag(name))
        } catch (e: DataIntegrityViolationException) {
            tagRepository.findByName(name) ?: throw e
        }
    }
}
