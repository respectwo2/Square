package org.shax3.square.domain.post.model;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import org.hibernate.annotations.SQLRestriction;
import org.shax3.square.common.entity.BaseTimeEntity;
import org.shax3.square.domain.user.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PROTECTED)
@Table(name = "post_comment")
@SQLRestriction("is_valid = true")
public class PostComment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "parent_id")
	private PostComment parent;

	@Column(nullable = false)
	private String content;

	@Column(name = "is_valid", nullable = false)
	private boolean valid;

	@Column(nullable = false)
	private int likeCount;

	@Builder
	public PostComment(User user, Post post, PostComment parent, String content) {
		this.user = user;
		this.post = post;
		this.parent = parent;
		this.content = content;
		this.valid = true;
		this.likeCount = 0;
	}

	public void updateContent(String content) {
		this.content = content;
	}

	public void softDelete() {
		valid = false;
	}

	public void increaseLikeCount(int countDiff) {
		this.likeCount += countDiff;
	}

}
