package org.shax3.square.domain.notification.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.shax3.square.common.model.DisplayableEnum;

public enum NotificationType implements DisplayableEnum {
    POST_COMMENT("게시글"),
    DEBATE_COMMENT("논쟁"),
    TODAY_DEBATE("오늘의 논쟁"),
    NOTICE("공지사항");

    private final String koreanName;

    NotificationType(String koreanName) {
        this.koreanName = koreanName;
    }

    @Override
    @JsonValue
    public String getKoreanName() {
        return koreanName;
    }
}
