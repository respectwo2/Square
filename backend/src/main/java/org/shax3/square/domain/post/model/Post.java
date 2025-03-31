package org.shax3.square.domain.post.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.shax3.square.domain.user.model.Type;
import org.shax3.square.domain.user.model.User;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PROTECTED)
@Table(name = "post")
@SQLRestriction("is_valid = true")
public class Post {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int likeCount;

    @Column(name = "is_valid", nullable = false)
    private boolean valid;

    @Enumerated(STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private int referenceCount;

    @Builder
    public Post(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.type = Type.COMMON; //추후 성향별 게시판 생기면 변경해야함
        this.valid = true;
        this.likeCount = 0;
        this.referenceCount = 0;
    }
}
