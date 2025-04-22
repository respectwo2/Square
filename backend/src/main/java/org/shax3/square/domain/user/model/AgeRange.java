package org.shax3.square.domain.user.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.shax3.square.common.model.DisplayableEnum;


public enum AgeRange implements DisplayableEnum {
    TEN("10대"),
    TWENTY("20대"),
    THIRTY("30대"),
    FORTY("40대"),
    FIFTY("50대 이상");

    private final String koreanName;

    AgeRange(String koreanName) {
        this.koreanName = koreanName;
    }

    @Override
    @JsonValue
    public String getKoreanName() {
        return koreanName;
    }
}
