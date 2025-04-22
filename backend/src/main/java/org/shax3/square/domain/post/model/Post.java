package org.shax3.square.domain.post.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.shax3.square.common.entity.BaseTimeEntity;
import org.shax3.square.domain.user.model.Type;
import org.shax3.square.domain.user.model.User;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PROTECTED)
@Table(name = "post")
@SQLRestriction("is_valid = true")
public class Post extends BaseTimeEntity {
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

    @Setter
    @OneToMany(mappedBy = "post", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<PostImage> postImages = new ArrayList<>();

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

    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;

    }

    public void addPostImage(PostImage postImage) {
        postImages.add(postImage);
        postImage.setPost(this);
    }

    public boolean removePostImage(PostImage postImage) {
        postImage.setPost(null);
        return postImages.remove(postImage);
    }

    public void softDelete() {
        valid = false;
    }

    public void increaseLikeCount(int countDiff) {
        this.likeCount += countDiff;
    }
}
