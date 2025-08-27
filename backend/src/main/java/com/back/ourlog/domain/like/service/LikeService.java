package com.back.ourlog.domain.like.service;

import com.back.ourlog.domain.diary.entity.Diary;
import com.back.ourlog.domain.diary.repository.DiaryRepository;
import com.back.ourlog.domain.like.entity.Like;
import com.back.ourlog.domain.like.repository.LikeRepository;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.repository.UserRepository;
import com.back.ourlog.global.rq.Rq;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final Rq rq;

    @Transactional
    public boolean like(Integer diaryId) {
        User currentUser = rq.getCurrentUser();

        if (likeRepository.existsByUserIdAndDiaryId(currentUser.getId(), diaryId)) {
            return false;
        }

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리 없음"));

        likeRepository.save(new Like(diary, currentUser));
        return true;
    }

    @Transactional  // 좋아요 삭제
    public void unlike(Integer diaryId) {
        User currentUser = rq.getCurrentUser();
        likeRepository.deleteByUserIdAndDiaryId(currentUser.getId(), diaryId);
    }

    public int getLikeCount(Integer diaryId) {
        return likeRepository.countByDiaryId(diaryId);
    }
}
