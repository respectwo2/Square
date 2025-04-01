package org.shax3.square.domain.post.dto;

import org.shax3.square.domain.post.model.Post;

public record PostSummaryDto(
	Long postId,
	String nickname,
	String profileUrl,
	String userType,
	String createdAt,
	String title,
	String content,
	int likeCount,
	int commentCount,
	boolean isLiked
) {
	public static PostSummaryDto from(Post post, String profileUrl, boolean isLiked, int commentCount) {
		return new PostSummaryDto(
			post.getId(),
			post.getUser().getNickname(),
			profileUrl,
			post.getUser().getType().name(),
			post.getCreatedAt().toString(),    // createdAt 필드 필요
			post.getTitle(),
			post.getContent(),
			post.getLikeCount(),
			commentCount,
			isLiked
		);
	}
}
