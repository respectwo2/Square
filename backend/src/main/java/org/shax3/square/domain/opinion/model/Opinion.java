package org.shax3.square.domain.opinion.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.shax3.square.common.entity.BaseTimeEntity;
import org.shax3.square.domain.debate.model.Debate;
import org.shax3.square.domain.user.model.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "opinion")
@SQLRestriction("is_valid = true")
public class Opinion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debate_id", nullable = false)
    private Debate debate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "is_valid", nullable = false)
    private boolean valid;

    @Column(name = "is_left", nullable = false)
    private boolean left;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    @Builder
    public Opinion(User user, Debate debate, boolean left, String content) {
        this.debate = debate;
        this.user = user;
        this.content = content;
        this.likeCount = 0;
        this.left = left;
        this.valid = true;
        this.commentCount = 0;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void softDelete() {
        this.valid = false;
    }

    public void increaseLikeCount(int countDiff) {
        this.likeCount += countDiff;
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        this.commentCount = Math.max(0, this.commentCount - 1); // 음수 방지
    }
}

