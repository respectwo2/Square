package org.shax3.square.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.shax3.square.domain.user.model.AgeRange;
import org.shax3.square.domain.user.model.Gender;
import org.shax3.square.domain.user.model.Region;
import org.shax3.square.domain.user.model.Religion;
import org.shax3.square.domain.user.model.SocialType;
import org.shax3.square.domain.user.model.User;

public record SignUpRequest(

        @NotBlank(message = "닉네임은 필수 요소입니다.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,8}$", message = "닉네임은 영문, 숫자, 한글로만 구성되어 있으며, 길이는 2자리 이상 8자리 이하이어야 합니다.")
        String nickname,
        String fileName,
        @NotBlank(message = "지역을 입력해주세요.")
        Region region,
        @NotBlank(message = "성별을 입력해주세요.")
        Gender gender,
        @NotBlank(message = "출생연도를 입력해주세요.")
        int yearOfBirth,
        @NotBlank(message = "종교를 입력해주세요.")
        Religion religion
) {
    public User to(String email, SocialType socialType, AgeRange ageRange) {
        return User.builder()
                .email(email)
                .nickname(this.nickname)
                .fileName(this.fileName)
                .region(this.region)
                .gender(this.gender)
                .yearOfBirth(this.yearOfBirth)
                .ageRange(ageRange)
                .religion(this.religion)
                .socialType(socialType)
                .build();
    }
}
