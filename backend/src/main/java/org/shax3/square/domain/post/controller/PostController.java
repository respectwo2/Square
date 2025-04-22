package org.shax3.square.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.auth.annotation.AuthUser;
import org.shax3.square.domain.post.dto.request.CreatePostRequest;
import org.shax3.square.domain.post.dto.request.UpdatePostRequest;
import org.shax3.square.domain.post.dto.response.MyPostResponse;
import org.shax3.square.domain.post.dto.response.PostDetailResponse;
import org.shax3.square.domain.post.dto.response.PostListResponse;
import org.shax3.square.domain.post.service.PostFacadeService;
import org.shax3.square.domain.post.service.PostService;
import org.shax3.square.domain.user.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "게시글 관련 API")
public class PostController {

    private final PostService postService;
    private final PostFacadeService postFacadeService;

    @Operation(
            summary = "게시글 생성 api",
            description = "게시글 제목, 내용, 이미지(선택)를 넣어주면 게시글을 생성해줍니다."
    )
    @PostMapping
    public ResponseEntity<Void> createPost(
            @AuthUser User user,
            @Valid @RequestBody CreatePostRequest createPostRequest
    ) {
        postService.createPost(createPostRequest, user);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "게시글 수정 api",
            description = "게시글 제목, 내용, 삭제 이미지, 추가 이미지 리스트를 넣어주면 게시글을 수정합니다."
    )
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
            @AuthUser User user,
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest updatePostRequest
    ) {
        postService.updatePost(user, postId, updatePostRequest);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "게시글 삭제 api",
            description = "path에 포함된 게시글 id로 해당 게시글을 삭제합니다."
    )
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthUser User user,
            @PathVariable Long postId
    ) {
        postService.deletePost(user, postId);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "게시글 목록 조회 api",
            description = "게시글 목록을 조회합니다. (페이징 처리)"
    )
    @GetMapping
    public ResponseEntity<PostListResponse> getPosts(
            @AuthUser User user,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) Long nextCursorId,
            @RequestParam(required = false) Integer nextCursorLikes,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PostListResponse response = postFacadeService.getPostList(user, sort, nextCursorId, nextCursorLikes, limit);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "게시글 상세 조회 api",
            description = "게시글 id로 게시글과 댓글 목록을 조회합니다. 댓글은 전부 불러오고, 대댓글만 페이징 처리합니다."
    )
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(
            @AuthUser User user,
            @PathVariable Long postId
    ) {
        PostDetailResponse response = postFacadeService.getPostDetail(user, postId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내가 작성한 게시글 목록 조회 api",
            description = "내가 작성한 게시글 목록을 조회합니다. (페이징 처리)"
    )
    @GetMapping("/my")
    public ResponseEntity<MyPostResponse> getMyPosts(
            @AuthUser User user,
            @RequestParam(required = false) Long nextCursorId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        MyPostResponse response = postFacadeService.getMyPostList(user, nextCursorId, limit);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내가 좋아요한 게시글 목록 조회 api",
            description = "내가 좋아요한 게시글 목록을 조회합니다. (페이징 처리)"
    )
    @GetMapping("/my-likes")
    public ResponseEntity<MyPostResponse> getMyLikedPosts(
            @AuthUser User user,
            @RequestParam(required = false) Long nextCursorId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        MyPostResponse response = postFacadeService.getMyLikedPostList(user, nextCursorId, limit);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내가 스크랩한 게시글 목록 조회 api",
            description = "내가 스크랩한 게시글 목록을 조회합니다. (페이징 처리)"
    )
    @GetMapping("/my-scraps")
    public ResponseEntity<MyPostResponse> getMyScrapPosts(
            @AuthUser User user,
            @RequestParam(required = false) Long nextCursorId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        MyPostResponse response = postFacadeService.getMyScrapPostList(user, nextCursorId, limit);

        return ResponseEntity.ok(response);
    }
}
