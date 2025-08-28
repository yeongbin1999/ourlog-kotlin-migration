package com.back.ourlog.domain.genre.entity

import com.back.ourlog.domain.diary.entity.Diary
import jakarta.persistence.*

@Entity
@IdClass(DiaryGenreId::class)
class DiaryGenre(

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    val diary: Diary,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    val genre: Genre
)
