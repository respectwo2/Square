package org.shax3.square.domain.opinion.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.shax3.square.common.entity.BaseTimeEntity;
import org.shax3.square.domain.user.model.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "opinion_comment")
@SQLRestriction("is_valid = true")
public class OpinionComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opinion_id", nullable = false)
    private Opinion opinion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "is_valid", nullable = false)
    private boolean valid = true;

    @Builder
    public OpinionComment(Opinion opinion, User user, String content) {
        this.opinion = opinion;
        this.user = user;
        this.content = content;
        this.likeCount = 0;
        this.valid = true;
    }

    public void softDelete() {
        valid = false;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void increaseLikeCount(int countDiff) {
        this.likeCount += countDiff;
    }
}
