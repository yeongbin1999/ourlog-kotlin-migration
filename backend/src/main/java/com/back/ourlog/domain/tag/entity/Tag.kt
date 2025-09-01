package com.back.ourlog.domain.tag.entity

import jakarta.persistence.*
import org.hibernate.annotations.BatchSize

@Entity
@BatchSize(size = 50)
class Tag(
    @Column(unique = true, nullable = false)
    var name: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0

    @OneToMany(mappedBy = "tag", cascade = [CascadeType.ALL], orphanRemoval = true)
    val diaryTags: MutableList<DiaryTag> = mutableListOf()

    protected constructor() : this("")
}
