package com.back.ourlog.domain.diary.entity;

import com.back.ourlog.domain.comment.entity.Comment;
import com.back.ourlog.domain.content.entity.Content;
import com.back.ourlog.domain.content.entity.ContentType;
import com.back.ourlog.domain.genre.entity.DiaryGenre;
import com.back.ourlog.domain.genre.entity.Genre;
import com.back.ourlog.domain.genre.service.GenreService;
import com.back.ourlog.domain.like.entity.Like;
import com.back.ourlog.domain.ott.entity.DiaryOtt;
import com.back.ourlog.domain.ott.entity.Ott;
import com.back.ourlog.domain.ott.repository.OttRepository;
import com.back.ourlog.domain.tag.entity.DiaryTag;
import com.back.ourlog.domain.tag.entity.Tag;
import com.back.ourlog.domain.tag.repository.TagRepository;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.external.library.service.LibraryService;
import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true) // User 완성되면 false 활성화
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    private String title;
    private String contentText;
    private Float rating;
    private Boolean isPublic;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaryTag> diaryTags = new ArrayList<>();

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaryGenre> diaryGenres = new ArrayList<>();

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaryOtt> diaryOtts = new ArrayList<>();

    public Diary(User user, Content content, String title, String contentText, Float rating, Boolean isPublic) {
        this.user = user;
        this.content = content;
        this.title = title;
        this.contentText = contentText;
        this.rating = rating;
        this.isPublic = isPublic;
    }

    public void update(String title, String contentText, float rating, boolean isPublic) {
        this.title = title;
        this.contentText = contentText;
        this.rating = rating;
        this.isPublic = isPublic;
    }

    public void updateTags(List<String> newTagNames, TagRepository tagRepository) {
        List<DiaryTag> current = this.getDiaryTags();
        List<String> currentNames = current.stream()
                .map(dt -> dt.getTag().getName())
                .toList();

        List<DiaryTag> toRemove = current.stream()
                .filter(dt -> !newTagNames.contains(dt.getTag().getName()))
                .toList();
        this.getDiaryTags().removeAll(toRemove);

        List<String> toAdd = newTagNames.stream()
                .filter(name -> !currentNames.contains(name))
                .toList();

        toAdd.forEach(tagName -> {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(new Tag(tagName)));
            this.getDiaryTags().add(new DiaryTag(this, tag));
        });
    }

    public void updateGenres(List<String> newGenreNames, GenreService genreService, LibraryService libraryService) {
        List<DiaryGenre> current = this.getDiaryGenres();
        List<String> currentNames = current.stream()
                .map(dg -> dg.getGenre().getName())
                .toList();

        ContentType contentType = this.getContent().getType();
        List<String> mappedGenreNames = newGenreNames.stream()
                .map(name -> contentType == ContentType.BOOK ? libraryService.mapKdcToGenre(name) : name)
                .toList();

        List<DiaryGenre> toRemove = current.stream()
                .filter(dg -> !mappedGenreNames.contains(dg.getGenre().getName()))
                .toList();
        this.getDiaryGenres().removeAll(toRemove);

        List<String> toAdd = mappedGenreNames.stream()
                .filter(name -> !currentNames.contains(name))
                .toList();

        toAdd.forEach(name -> {
            Genre genre = genreService.findOrCreateByName(name);
            this.getDiaryGenres().add(new DiaryGenre(this, genre));
        });
    }

    public void updateOtts(List<Integer> newOttIds, OttRepository ottRepository) {
        if (this.getContent().getType() != ContentType.MOVIE) {
            this.diaryOtts.clear();
            return;
        }

        if (newOttIds == null) {
            newOttIds = new ArrayList<>();
        }

        this.diaryOtts.clear();

        for (Integer ottId : newOttIds) {
            Ott ott = ottRepository.findById(ottId)
                    .orElseThrow(() -> new CustomException(ErrorCode.OTT_NOT_FOUND));
            this.diaryOtts.add(new DiaryOtt(this, ott));
        }
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public Comment addComment(User user, String content) {
        Comment comment = new Comment(this, user, content);
        comments.add(comment);

        return comment;
    }

    public void deleteComment(Comment comment) {
        comments.remove(comment);
        comment.removeDiary();
    }
}