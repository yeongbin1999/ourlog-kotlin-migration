package com.back.ourlog.domain.ott.entity

import com.back.ourlog.domain.diary.entity.Diary
import java.io.Serializable

data class DiaryOttId(
    var diary: Diary? = null,
    var ott: Ott? = null
) : Serializable
