package com.back.ourlog.domain.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportReason {
    SPAM("스팸"),
    INAPPROPRIATE("부적절한 내용"),
    HARASSMENT("괴롭힘"),
    HATE_SPEECH("혐오 발언"),
    ETC("기타");

    private final String description;
}

