package org.shax3.square.domain.type.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.shax3.square.domain.user.model.User;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PROTECTED)
@Table(
        name = "type_result"
)
public class TypeResult {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(STRING)
    @Column(nullable = false)
    private Type1 type1;

    @Column(nullable = false)
    private int score1;

    @Enumerated(STRING)
    @Column(nullable = false)
    private Type2 type2;

    @Column(nullable = false)
    private int score2;

    @Enumerated(STRING)
    @Column(nullable = false)
    private Type3 type3;

    @Column(nullable = false)
    private int score3;

    @Enumerated(STRING)
    @Column(nullable = false)
    private Type4 type4;

    @Column(nullable = false)
    private int score4;

    @Builder
    public TypeResult(User user, Type1 type1, Type2 type2, Type3 type3, Type4 type4, int[] score) {
        this.user = user;
        this.type1 = type1;
        this.type2 = type2;
        this.type3 = type3;
        this.type4 = type4;
        this.score1 = score[0];
        this.score2 = score[1];
        this.score3 = score[2];
        this.score4 = score[3];
    }

    public void updateType(Type1 type1, Type2 type2, Type3 type3, Type4 type4, int[] score) {
        this.type1 = type1;
        this.type2 = type2;
        this.type3 = type3;
        this.type4 = type4;
        this.score1 = score[0];
        this.score2 = score[1];
        this.score3 = score[2];
        this.score4 = score[3];
    }
}
