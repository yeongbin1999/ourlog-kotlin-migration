package com.back.ourlog.domain.ott.entity

import com.back.ourlog.domain.diary.entity.Diary
import jakarta.persistence.*

@Entity
@IdClass(DiaryOttId::class)
class DiaryOtt(

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    val diary: Diary,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ott_id", nullable = false)
    val ott: Ott
)