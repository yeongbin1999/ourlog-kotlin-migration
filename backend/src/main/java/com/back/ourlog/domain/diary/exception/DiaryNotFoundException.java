package com.back.ourlog.domain.diary.exception;

import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;

public class DiaryNotFoundException extends CustomException {
    public DiaryNotFoundException() {
        super(ErrorCode.DIARY_NOT_FOUND);
    }
}

