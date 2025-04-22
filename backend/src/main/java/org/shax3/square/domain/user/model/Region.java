package org.shax3.square.domain.user.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.shax3.square.common.model.DisplayableEnum;

public enum Region implements DisplayableEnum {
    SEOUL("서울특별시"),
    GYEONGGI("경기도"),
    INCHEON("인천광역시"),
    BUSAN("부산광역시"),
    DAEGU("대구광역시"),
    DAEJEON("대전광역시"),
    GWANGJU("광주광역시"),
    ULSAN("울산광역시"),
    SEJONG("세종특별자치시"),
    CHUNGBUK("충청북도"),
    CHUNGNAM("충청남도"),
    JEONNAM("전라남도"),
    JEONBUK("전북특별자치도"),
    GYEONGBUK("경상북도"),
    GYEONGNAM("경상남도"),
    GANGWON("강원특별자치도"),
    JEJU("제주특별자치도");

    private final String koreanName;

    Region(String koreanName) {
        this.koreanName = koreanName;
    }

    @Override
    @JsonValue
    public String getKoreanName() {
        return koreanName;
    }
}
