package com.back.ourlog.domain.comment.service;

import com.back.ourlog.domain.comment.dto.CommentResponseDto;
import com.back.ourlog.domain.comment.entity.Comment;
import com.back.ourlog.domain.comment.repository.CommentRepository;
import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.diary.repository.DiaryRepository;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.repository.UserRepository;
import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final DiaryRepository diaryRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    @Transactional
    public CommentResponseDto write(int diaryId, User user, String content) {
        if(user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));



        Comment comment = diary.addComment(user, content);
        diaryRepository.flush();

        return new CommentResponseDto(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(int diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));

        // 최신 순으로 나열된 댓글 정보
        List<Comment> comments = commentRepository.findByDiaryOrderByCreatedAtDesc(diary);

        return comments.stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(int id, String content) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        comment.update(content);
    }

    @Transactional
    public void delete(int id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 양방향(User, Diary) 관계 제거
        Diary diary = comment.getDiary();
        diary.deleteComment(comment);

        User user = comment.getUser();

        user.deleteComment(comment);

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public void checkCanDelete(User user, int commentId) {
        if(user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if(!comment.getUser().equals(user)) {
            throw new CustomException(ErrorCode.COMMENT_DELETE_FORBIDDEN);
        }
    }

    @Transactional(readOnly = true)
    public void checkCanUpdate(User user, int commentId) {
        if(user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if(!comment.getUser().equals(user)) {
            throw new CustomException(ErrorCode.COMMENT_UPDATE_FORBIDDEN);
        }
    }
}
