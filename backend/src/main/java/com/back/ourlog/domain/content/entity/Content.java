package com.back.ourlog.domain.content.entity;

import com.back.ourlog.domain.content.dto.ContentSearchResultDto;
import com.back.ourlog.domain.diary.entity.Diary;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Content {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Integer id;

    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType type;

    @Column(name = "creator_name")
    private String creatorName;

    @Column(length = 1000)
    private String description;

    private String posterUrl;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime releasedAt;
    private String externalId;

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Diary> diaries = new ArrayList<>();

    public Content(String title, ContentType type, String creatorName, String description, String posterUrl, LocalDateTime releasedAt, String externalId) {
        this.title = title;
        this.type = type;
        this.creatorName = creatorName;
        this.description = description;
        this.posterUrl = posterUrl;
        this.releasedAt = releasedAt;
        this.externalId = externalId;
    }

    public void update(String externalId, ContentType type) {
        this.externalId = externalId;
        this.type = type;
    }

    public static Content of(ContentSearchResultDto result) {
        // 영화(MOVIE)일 때만 description 저장
        String description = result.type() == ContentType.MOVIE ? result.description() : null;

        return new Content(
                result.title(),
                result.type(),
                result.creatorName(),
                description,
                result.posterUrl(),
                result.releasedAt(),
                result.externalId()
        );
    }


}
