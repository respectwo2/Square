package org.shax3.square.domain.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.shax3.square.common.entity.BaseTimeEntity;
import org.shax3.square.domain.user.model.User;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    private Long targetId;

    @Column(name = "is_read")
    private boolean read;

    @Builder
    public Notification(User user, String title, String message, NotificationType type, Long targetId) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.targetId = targetId;
        this.read = false;
    }

    public void markAsRead() {
        this.read = true;
    }
}
