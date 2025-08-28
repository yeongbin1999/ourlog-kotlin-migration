package com.back.ourlog.domain.ott.entity

import jakarta.persistence.*

@Entity
class Ott(

    @Column(nullable = false, unique = true)
    val name: String,
    val logoUrl: String? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null

    @OneToMany(mappedBy = "ott", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val diaryOtts: MutableList<DiaryOtt> = mutableListOf()

    override fun equals(other: Any?): Boolean =
        this === other || (other is Ott && id != null && id == other.id)

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
