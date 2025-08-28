package com.back.ourlog.domain.ott.entity;

import com.back.ourlog.domain.diary.entity.Diary;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
@IdClass(DiaryOttId.class)
public class DiaryOtt {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ott_id", nullable = false)
    private Ott ott;

    public DiaryOtt(Diary diary, Ott ott) {
        this.diary = diary;
        this.ott = ott;
    }

    public void setDiary(Diary diary) {
        this.diary = diary;
    }

    public void setOtt(Ott ott) {
        this.ott = ott;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiaryOtt)) return false;
        DiaryOtt diaryOtt = (DiaryOtt) o;
        return Objects.equals(diary.getId(), diaryOtt.diary.getId()) &&
                Objects.equals(ott.getId(), diaryOtt.ott.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(diary.getId(), ott.getId());
    }
}
