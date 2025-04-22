package org.shax3.square.domain.user.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.shax3.square.common.model.DisplayableEnum;

public enum Gender implements DisplayableEnum {
    MALE("남성"),
    FEMALE("여성"),
    NONE("알리지 않음");

    private final String koreanName;

    Gender(String koreanName) {
        this.koreanName = koreanName;
    }

    @Override
    @JsonValue
    public String getKoreanName() {
        return koreanName;
    }
}
