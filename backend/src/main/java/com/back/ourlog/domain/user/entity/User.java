package com.back.ourlog.domain.user.entity;

import com.back.ourlog.domain.banHistory.entity.BanHistory;
import com.back.ourlog.domain.comment.entity.Comment;
import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.follow.entity.Follow;
import com.back.ourlog.domain.like.entity.Like;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "providerId"})
        }
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, length = 50)
    private String email;

    @Column(length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    private String profileImageUrl;

    private String bio;

    @Column(length = 20)
    private String provider;

    @Column(length = 100)
    private String providerId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Role role = Role.USER;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<Diary> diaries = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Like> likes = new ArrayList<>();

    // 내가 팔로우 한 사람 (팔로잉)
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Follow> followings = new ArrayList<>();

    // 나를 팔로우 한 사람 (팔로워)
    @OneToMany(mappedBy = "followee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Follow> followers = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Integer followingsCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer followersCount = 0;

    public void increaseFollowersCount() {
        this.followersCount++;
    }

    public void decreaseFollowersCount() {
        if (this.followersCount > 0) this.followersCount--;
    }

    public void increaseFollowingsCount() {
        this.followingsCount++;
    }

    public void decreaseFollowingsCount() {
        if (this.followingsCount > 0) this.followingsCount--;
    }

    public void deleteComment(Comment comment) {
        comments.remove(comment);
        comment.removeUser();
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BanHistory> banHistories = new ArrayList<>();

    public boolean isCurrentlyBanned() {
        return banHistories.stream()
                .anyMatch(BanHistory::isActiveNow);
    }

    // === 일반 가입 전용 생성자 ===
    public static User createNormalUser(String email, String encodedPassword, String nickname, String profileImageUrl, String bio) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .bio(bio)
                .provider("local")
                .providerId(email)
                .build();
    }

    // === 소셜 가입 전용 생성자 ===
    public static User createSocialUser(String provider, String providerId, String email, String nickname, String profileImageUrl) {
        return User.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
