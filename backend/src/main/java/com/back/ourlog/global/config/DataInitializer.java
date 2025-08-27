package com.back.ourlog.global.config;

import com.back.ourlog.domain.comment.entity.Comment;
import com.back.ourlog.domain.comment.repository.CommentRepository;
import com.back.ourlog.domain.content.entity.Content;
import com.back.ourlog.domain.content.entity.ContentType;
import com.back.ourlog.domain.content.repository.ContentRepository;
import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.diary.repository.DiaryRepository;
import com.back.ourlog.domain.follow.entity.Follow;
import com.back.ourlog.domain.follow.repository.FollowRepository;
import com.back.ourlog.domain.genre.entity.DiaryGenre;
import com.back.ourlog.domain.genre.entity.Genre;
import com.back.ourlog.domain.genre.repository.GenreRepository;
import com.back.ourlog.domain.like.entity.Like;
import com.back.ourlog.domain.like.repository.LikeRepository;
import com.back.ourlog.domain.ott.entity.DiaryOtt;
import com.back.ourlog.domain.ott.entity.Ott;
import com.back.ourlog.domain.ott.repository.OttRepository;
import com.back.ourlog.domain.tag.entity.DiaryTag;
import com.back.ourlog.domain.tag.entity.Tag;
import com.back.ourlog.domain.tag.repository.TagRepository;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final DiaryRepository diaryRepository;
    private final FollowRepository followRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final TagRepository tagRepository;
    private final OttRepository ottRepository;
    private final GenreRepository genreRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random(42); // 항상 동일한 랜덤 패턴

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            System.out.println("✅ 기존 데이터가 있어서 더미 데이터 생성을 건너뜁니다.");
            return;
        }

        System.out.println("🚀 더미 데이터 생성 시작!");

        List<Tag> tags = createTags();
        List<Genre> genres = createGenres();
        List<Ott> otts = createOtts();
        List<User> users = createUsers(20);
        List<Content> contents = createContents(30);
        List<Diary> diaries = createDiaries(users, contents, 50);
        createFollows(users);
        createCommentsAndLikes(users, diaries);
        attachTagsGenresOtts(diaries, tags, genres, otts);

        System.out.println("✅ 더미 데이터 생성 완료!");
    }

    private List<Tag> createTags() {
        return tagRepository.saveAll(Arrays.asList("힐링", "감동", "로맨스", "스릴러", "코미디", "액션", "음악", "철학").stream().map(Tag::new).toList());
    }

    private List<Genre> createGenres() {
        return genreRepository.saveAll(Arrays.asList("드라마", "SF", "판타지", "애니메이션", "다큐멘터리").stream().map(Genre::new).toList());
    }

    private List<Ott> createOtts() {
        return ottRepository.saveAll(Arrays.asList("Netflix", "Disney+", "Prime Video", "TVING", "Watcha").stream()
                .map(name -> new Ott(name, "https://logo.com/" + name.toLowerCase().replace("+", "")))
                .toList());
    }

    private List<User> createUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String email = "user" + i + "@test.com";
            String password = passwordEncoder.encode("password" + i);
            String nickname = "유저" + i;
            String profile = "https://picsum.photos/200?random=" + i;
            String bio = "안녕하세요! 저는 " + nickname + " 입니다.";
            users.add(User.createNormalUser(email, password, nickname, profile, bio));
        }
        return userRepository.saveAll(users);
    }

    private List<Content> createContents(int count) {
        List<Content> contents = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String title = "콘텐츠 " + i;
            ContentType type = ContentType.values()[random.nextInt(ContentType.values().length)];
            String creatorName = "제작자 " + (i % 5 + 1);
            String description = "이것은 " + title + " 에 대한 설명입니다.";
            String posterUrl = "https://picsum.photos/300?content=" + i;
            LocalDateTime releasedAt = LocalDateTime.now().minusDays(random.nextInt(1000));
            String externalId = "EXT-" + i;
            contents.add(new Content(title, type, creatorName, description, posterUrl, releasedAt, externalId));
        }
        return contentRepository.saveAll(contents);
    }

    private List<Diary> createDiaries(List<User> users, List<Content> contents, int count) {
        List<Diary> diaries = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            User user = users.get(random.nextInt(users.size()));
            Content content = contents.get(random.nextInt(contents.size()));
            String title = "다이어리 " + i;
            String contentText = "이것은 다이어리 " + i + "의 본문 내용입니다.";
            float rating = random.nextInt(5) + 1;
            boolean isPublic = random.nextBoolean();
            diaries.add(new Diary(user, content, title, contentText, rating, isPublic));
        }
        return diaryRepository.saveAll(diaries);
    }

    private void createFollows(List<User> users) {
        List<Follow> follows = new ArrayList<>();
        for (User follower : users) {
            int followCount = 3 + random.nextInt(3);
            Set<User> alreadyFollowed = new HashSet<>();
            for (int i = 0; i < followCount; i++) {
                User followee = users.get(random.nextInt(users.size()));
                if (!followee.equals(follower) && alreadyFollowed.add(followee)) {
                    follows.add(new Follow(follower, followee));
                    follower.increaseFollowingsCount();
                    followee.increaseFollowersCount();
                }
            }
        }
        followRepository.saveAll(follows);
    }

    private void createCommentsAndLikes(List<User> users, List<Diary> diaries) {
        List<Comment> comments = new ArrayList<>();
        List<Like> likes = new ArrayList<>();

        for (Diary diary : diaries) {
            int commentCount = 2 + random.nextInt(3);
            for (int i = 0; i < commentCount; i++) {
                User commenter = users.get(random.nextInt(users.size()));
                comments.add(new Comment(diary, commenter, "이 다이어리 정말 좋네요! " + UUID.randomUUID()));
            }

            int likeCount = 1 + random.nextInt(5);
            Set<User> alreadyLiked = new HashSet<>();
            for (int i = 0; i < likeCount; i++) {
                User liker = users.get(random.nextInt(users.size()));
                if (alreadyLiked.add(liker)) {
                    likes.add(new Like(diary, liker));
                }
            }
        }
        commentRepository.saveAll(comments);
        likeRepository.saveAll(likes);
    }

    private void attachTagsGenresOtts(List<Diary> diaries, List<Tag> tags, List<Genre> genres, List<Ott> otts) {
        for (Diary diary : diaries) {
            Collections.shuffle(tags);
            diary.getDiaryTags().addAll(tags.subList(0, 1 + random.nextInt(3)).stream()
                    .map(tag -> new DiaryTag(diary, tag)).toList());

            Collections.shuffle(genres);
            diary.getDiaryGenres().addAll(genres.subList(0, 1 + random.nextInt(2)).stream()
                    .map(genre -> new DiaryGenre(diary, genre)).toList());

            Collections.shuffle(otts);
            diary.getDiaryOtts().addAll(otts.subList(0, 1 + random.nextInt(2)).stream()
                    .map(ott -> new DiaryOtt(diary, ott)).toList());
        }
        diaryRepository.saveAll(diaries);
    }
}
