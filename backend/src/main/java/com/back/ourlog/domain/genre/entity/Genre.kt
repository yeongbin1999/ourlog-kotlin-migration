package com.back.ourlog.domain.genre.entity

import jakarta.persistence.*

@Entity
class Genre(

    @Column(unique = true, nullable = false)
    var name: String

) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null

    @OneToMany(
        mappedBy = "genre",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val diaryGenres: MutableList<DiaryGenre> = mutableListOf()

    override fun equals(other: Any?): Boolean =
        this === other || (other is Genre && id != null && id == other.id)

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
