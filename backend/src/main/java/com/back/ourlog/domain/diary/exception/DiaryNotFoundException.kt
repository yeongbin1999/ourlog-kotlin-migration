package com.back.ourlog.domain.diary.exception

import com.back.ourlog.global.exception.CustomException
import com.back.ourlog.global.exception.ErrorCode

class DiaryNotFoundException : CustomException(ErrorCode.DIARY_NOT_FOUND)