package org.shax3.square.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.auth.annotation.AuthUser;
import org.shax3.square.domain.post.dto.request.CreatePostCommentRequest;
import org.shax3.square.domain.post.dto.request.UpdatePostCommetRequest;
import org.shax3.square.domain.post.dto.response.MyCommentListResponse;
import org.shax3.square.domain.post.dto.response.PostCommentResponse;
import org.shax3.square.domain.post.dto.response.RepliesResponse;
import org.shax3.square.domain.post.service.CommentFacadeService;
import org.shax3.square.domain.post.service.PostCommentService;
import org.shax3.square.domain.user.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Tag(name = "PostComment", description = "게시글 댓글 관련 API")
public class PostCommentController {

    private final PostCommentService postCommentService;
    private final CommentFacadeService commentFacadeService;

    @Operation(
            summary = "게시글 댓글 생성 api",
            description = "게시글 댓글을 생성합니다."
    )
    @PostMapping
    public ResponseEntity<PostCommentResponse> createPostComment(
            @AuthUser User user,
            @Valid @RequestBody CreatePostCommentRequest request
    ) {
        PostCommentResponse response = postCommentService.createPostComment(request, user);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "게시글 댓글 수정 api",
            description = "게시글 댓글을 수정합니다."
    )
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updatePostComment(
            @AuthUser User user,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdatePostCommetRequest request
    ) {
        postCommentService.updatePostComment(user, request, commentId);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "게시글 댓글 삭제 api",
            description = "게시글 댓글을 삭제합니다."
    )
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deletePostComment(
            @AuthUser User user,
            @PathVariable Long commentId
    ) {
        postCommentService.deletePostComment(user, commentId);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "대댓글 목록 조회 (더보기) api",
            description = "대댓글 목록을 조회합니다. 더보기를 누를 때 사용합니다."
    )
    @GetMapping("/{commentId}")
    public ResponseEntity<RepliesResponse> getReplyList(
            @AuthUser User user,
            @PathVariable Long commentId,
            @RequestParam(required = false) Long nextCursorId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        RepliesResponse response = commentFacadeService.getReplies(user, commentId, nextCursorId, limit);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내가 작성한 댓글 목록 조회 api",
            description = "내가 작성한 댓글 목록을 조회합니다."
    )
    @GetMapping("/my")
    public ResponseEntity<MyCommentListResponse> getMyCommentList(
            @AuthUser User user,
            @RequestParam(required = false) Long nextCursorId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        MyCommentListResponse response = commentFacadeService.getMyComments(user, nextCursorId, limit);

        return ResponseEntity.ok(response);
    }
}
