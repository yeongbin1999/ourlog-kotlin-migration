package com.back.ourlog.domain.ott.entity;

import com.back.ourlog.domain.diary.entity.Diary;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DiaryOttId implements Serializable {
    private Diary diary;
    private Ott ott;
}
