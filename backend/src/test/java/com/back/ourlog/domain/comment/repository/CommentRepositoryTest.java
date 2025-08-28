//package com.back.ourlog.domain.comment.repository;
//
//import com.back.ourlog.domain.comment.entity.Comment;
//import com.back.ourlog.domain.diary.entity.Diary;
//import com.back.ourlog.domain.diary.repository.DiaryRepository;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@SpringBootTest
//@Transactional
//@ActiveProfiles("test")
//public class CommentRepositoryTest {
//    @Autowired
//    private DiaryRepository diaryRepository;
//    @Autowired
//    private CommentRepository commentRepository;
//
//    @Test
//    @DisplayName("댓글 최신 순으로 나열")
//    @Transactional(readOnly = true)
//    void t1() {
//        Diary diary = diaryRepository.findById(1).get();
//
//        List<Comment> comments = commentRepository.findQByDiaryOrderByCreatedAtDesc(diary);
//
//        Assertions.assertThat(comments.get(0).createdAt).isAfter(comments.get(1).createdAt);
//    }
//}
