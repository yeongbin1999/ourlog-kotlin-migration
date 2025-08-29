package com.back.ourlog.domain.tag.entity

import jakarta.persistence.*

@Entity
class Tag(
    @Column(unique = true, nullable = false)
    var name: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null

    @OneToMany(mappedBy = "tag", cascade = [CascadeType.ALL], orphanRemoval = true)
    val diaryTags: MutableList<DiaryTag> = mutableListOf()

    protected constructor() : this("")
}
