package com.back.ourlog.domain.tag.entity

import com.back.ourlog.domain.diary.entity.Diary
import jakarta.persistence.*

@Entity
@IdClass(DiaryTagId::class)
class DiaryTag(

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    var diary: Diary,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    var tag: Tag
) {
    protected constructor() : this(
        diary = Diary(), // Dummy
        tag = Tag("")
    )
}
