package org.shax3.square.domain.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.shax3.square.common.entity.BaseTimeEntity;
import org.shax3.square.domain.user.dto.request.UpdateProfileRequest;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static org.shax3.square.domain.user.model.State.ACTIVE;
import static org.shax3.square.domain.user.model.State.LEAVE;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PROTECTED)
@Table(
        name = "user"
)
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;

    @Email
    @Column(name = "email", length = 40, unique = true)
    private String email;

    @Column(name = "nickname", nullable = false, length = 10, unique = true)
    private String nickname;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Enumerated(STRING)
    @Column(name = "region", nullable = false)
    private Region region;

    @Enumerated(STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "year_of_birth", nullable = false)
    private int yearOfBirth;

    @Enumerated(STRING)
    @Column(name = "age_range", nullable = false)
    private AgeRange ageRange;

    @Enumerated(STRING)
    @Column(name = "religion", nullable = false)
    private Religion religion;

    @Enumerated(STRING)
    @Column(name = "type")
    private Type type;

    @Enumerated(STRING)
    @Column(name = "social_type", nullable = false)
    private SocialType socialType;

    @Enumerated(STRING)
    @Column(name = "state")
    private State state;

    @Builder
    public User(
            String email,
            String nickname,
            String s3Key,
            Region region,
            Gender gender,
            int yearOfBirth,
            AgeRange ageRange,
            Religion religion,
            Type type,
            SocialType socialType
    ) {
        this.email = email;
        this.nickname = nickname;
        this.s3Key = s3Key;
        this.region = region;
        this.gender = gender;
        this.yearOfBirth = yearOfBirth;
        this.ageRange = ageRange;
        this.religion = religion;
        this.type = type;
        this.socialType = socialType;
        this.state = ACTIVE;
    }

    public void updateProfileInfo(UpdateProfileRequest updateProfileRequest, String s3Key) {
        this.nickname = updateProfileRequest.nickname();
        this.s3Key = s3Key;
        this.region = updateProfileRequest.region();
        this.religion = updateProfileRequest.religion();
    }

    public void updateType(Type type) {
        this.type = type;
    }

    public void deleteAccount() {
        this.state = LEAVE;
    }
}
