package org.shax3.square.domain.user.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.shax3.square.common.model.DisplayableEnum;

public enum Religion implements DisplayableEnum {
    NONE("없음"),
    CHRISTIAN("기독교"),
    BUDDHISM("불교"),
    CATHOLIC("천주교"),
    OTHER("기타");

    private final String koreanName;

    Religion(String koreanName) {
        this.koreanName = koreanName;
    }

    @Override
    @JsonValue
    public String getKoreanName() {
        return koreanName;
    }
}
