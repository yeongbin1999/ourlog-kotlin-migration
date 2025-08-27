package com.back.ourlog.domain.timeline.service;

import com.back.ourlog.domain.comment.repository.CommentRepository;
import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.like.repository.LikeRepository;
import com.back.ourlog.domain.timeline.dto.TimelineResponse;
import com.back.ourlog.domain.timeline.repository.TimelineRepository;
import com.back.ourlog.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// 공개된 일기 목록 조회, 좋아요 수, 좋아요 여부, 댓글 수, 유저 정보 DTO -> 프론트 전달..
@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TimelineRepository timelineRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final Rq rq;


    public List<TimelineResponse> getPublicTimeline() {

        Integer currentUserIdTemp;
        try {
            currentUserIdTemp = rq.getCurrentUser().getId();
        } catch (Exception ignored) {
            currentUserIdTemp = null;
        }

        final Integer currentUserId = currentUserIdTemp;

        List<Diary> diaries = timelineRepository.findPublicDiaries();

        return diaries.stream()
                .map(diary -> new TimelineResponse(
                        diary.getId(),
                        diary.getTitle(),
                        diary.getContentText(),
                        diary.getCreatedAt().toString(),
                        diary.getContent().getPosterUrl(),
                        likeRepository.countByDiaryId(diary.getId()),   // 좋아요 개수..
                        commentRepository.countByDiaryId(diary.getId()),    // 댓글 개수..
                        currentUserId != null && likeRepository.existsByUserIdAndDiaryId(currentUserId, diary.getId()),
                        new TimelineResponse.UserSummary(
                                diary.getUser().getId(),
                                diary.getUser().getNickname(),
                                diary.getUser().getProfileImageUrl()
                        )
                ))
                .toList();

    }
}
