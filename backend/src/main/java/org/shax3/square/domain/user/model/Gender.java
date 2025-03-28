package org.shax3.square.domain.user.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MALE("남성"),
    FEMALE("여성"),
    NONE("알리지 않음");

    private final String koreanName;
    Gender(String koreanName) {
        this.koreanName = koreanName;
    }
    @JsonValue
    public String getKoreanName() {
        return koreanName;
    }
}
