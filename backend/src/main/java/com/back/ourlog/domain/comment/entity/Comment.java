package com.back.ourlog.domain.comment.entity;

import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String content;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true) // User 완성되면 nullable 활성화
    private User user;

    public Comment(Diary diary, User user, String content) {
        this.user = user;
        this.content = content;
        this.diary = diary;
    }

    public void update(String content) {
        this.content = content;
    }

    public void removeDiary() {
        this.diary = null;
    }

    public void removeUser() {
        this.user = null;
    }
}
