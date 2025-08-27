package com.back.ourlog.domain.follow.entity;

import com.back.ourlog.domain.follow.enums.FollowStatus;
import com.back.ourlog.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "followee_id", nullable = false)
    private User followee;

    @Enumerated(EnumType.STRING) // Enum을 DB에 저장할 때 문자열로 저장되게 함..
    @Column(nullable = false)
    private FollowStatus status = FollowStatus.PENDING;

    public void accept() {
        System.out.println("[DEBUG] 이전 상태: " + this.status);
        this.status = FollowStatus.ACCEPTED;
        System.out.println("[DEBUG] 변경 후 상태: " + this.status);
    }

    public void reject() {
        this.status = FollowStatus.REJECTED;
    }

    public Follow(User following, User followee) {
        this.follower = following;
        this.followee = followee;
    }
}
