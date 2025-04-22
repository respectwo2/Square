package org.shax3.square.domain.proposal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.shax3.square.domain.user.model.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "proposal")
@SQLRestriction("is_valid = true")
public class Proposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String topic;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "is_valid", nullable = false)
    private boolean valid;

    @Builder
    public Proposal(User user, String topic) {
        this.user = user;
        this.topic = topic;
        this.likeCount = 0;
        this.valid = true;
    }

    public void softDelete() {
        this.valid = false;
    }

    public void increaseLikeCount(int countDiff) {
        this.likeCount += countDiff;
    }
}
