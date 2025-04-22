package org.shax3.square.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.shax3.square.domain.post.model.Post;
import org.shax3.square.domain.post.model.PostComment;
import org.shax3.square.domain.user.model.User;

public record CreatePostCommentRequest(
        @NotBlank(message = "댓글 내용은 비어 있을 수 없습니다.")
        @Size(min = 5, max = 150, message = "댓글은 최소 5자 이상, 최대 150자 이하이어야 합니다.")
        String content,

        @NotNull
        Long postId,
        Long parentId
) {
    public PostComment to(Post post, PostComment postComment, User user) {
        return PostComment.builder()
                .content(this.content)
                .post(post)
                .parent(postComment)
                .user(user)
                .build();
    }
}
