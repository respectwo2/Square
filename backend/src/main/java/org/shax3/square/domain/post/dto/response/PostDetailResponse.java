package org.shax3.square.domain.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.shax3.square.domain.post.dto.CommentDto;
import org.shax3.square.domain.post.dto.PostImageDto;
import org.shax3.square.domain.post.model.Post;

public record PostDetailResponse(
	Long postId,
	List<PostImageDto> images,
	String userType,
	String nickname,
	LocalDateTime createdAt,
	String title,
	String content,
	int likeCount,
	int commentCount,
	boolean isLiked,
	boolean isScrapped,
	List<CommentDto> comments
) {
	public static PostDetailResponse from(
		Post post,
		List<PostImageDto> images,
		boolean isLiked,
		boolean isScrapped,
		List<CommentDto> comments,
		int commentCount
	) {
		return new PostDetailResponse(
			post.getId(),
			images,
			post.getUser().getType().name(),
			post.getUser().getNickname(),
			post.getCreatedAt(),
			post.getTitle(),
			post.getContent(),
			post.getLikeCount(),
			commentCount,
			isLiked,
			isScrapped,
			comments
		);
	}
}
