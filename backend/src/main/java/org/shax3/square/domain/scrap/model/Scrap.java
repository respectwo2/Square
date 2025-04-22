package org.shax3.square.domain.scrap.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.shax3.square.common.entity.BaseTimeEntity;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.user.model.User;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PROTECTED)
@Table(
        name = "scrap"
)
public class Scrap extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "target_id", nullable = false)
    Long targetId;

    @Column(name = "target_type", nullable = false)
    TargetType targetType;

    @Builder
    public Scrap(User user, Long targetId, TargetType targetType) {
        this.user = user;
        this.targetId = targetId;
        this.targetType = targetType;
    }
}
