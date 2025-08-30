package com.back.ourlog.domain.tag.entity

import com.back.ourlog.domain.diary.entity.Diary
import java.io.Serializable

data class DiaryTagId(
    var diary: Diary? = null,
    var tag: Tag? = null
) : Serializable
