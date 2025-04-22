package org.shax3.square.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.shax3.square.domain.user.model.*;

public record SignUpRequest(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotNull(message = "소셜 타입은 필수입니다.")
        SocialType socialType,

        @NotBlank(message = "닉네임은 필수 요소입니다.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,8}$", message = "닉네임은 영문, 숫자, 한글로만 구성되어 있으며, 길이는 2자리 이상 8자리 이하이어야 합니다.")
        String nickname,
        String s3Key,
        @NotNull(message = "지역을 입력해주세요.")
        Region region,
        @NotNull(message = "성별을 입력해주세요.")
        Gender gender,
        @NotNull(message = "출생연도를 입력해주세요.")
        int yearOfBirth,
        @NotNull(message = "종교를 입력해주세요.")
        Religion religion
) {
    public User to(AgeRange ageRange, String s3Key) {
        return User.builder()
                .email(this.email)
                .nickname(this.nickname)
                .s3Key(s3Key)
                .region(this.region)
                .gender(this.gender)
                .yearOfBirth(this.yearOfBirth)
                .ageRange(ageRange)
                .religion(this.religion)
                .socialType(socialType)
                .build();
    }
}
