package org.shax3.square.domain.post.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shax3.square.domain.post.dto.request.CreatePostCommentRequest;
import org.shax3.square.domain.post.dto.request.UpdatePostCommetRequest;
import org.shax3.square.domain.post.dto.response.PostCommentResponse;
import org.shax3.square.domain.post.model.Post;
import org.shax3.square.domain.post.model.PostComment;
import org.shax3.square.domain.post.repository.PostCommentRepository;
import org.shax3.square.domain.s3.service.S3Service;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.exception.CustomException;
import org.shax3.square.exception.ExceptionCode;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceTest {

    @Mock
    private PostCommentRepository postCommentRepository;

    @Mock
    private PostService postService;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private PostCommentService postCommentService;

    private User user;
    private Post post;
    private PostComment comment;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .nickname("test-user")
                .s3Key("user/profile.jpg")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        post = Post.builder()
                .title("test")
                .content("hi")
                .user(user)
                .build();
        ReflectionTestUtils.setField(post, "id", 100L);

        comment = PostComment.builder()
                .user(user)
                .post(post)
                .content("수정 전")
                .build();
        ReflectionTestUtils.setField(comment, "id", 1L);
    }

    @Test
    @DisplayName("댓글 생성 성공 - 부모 댓글이 없는 경우")
    void commentCreationSuccessWithoutParent() {
        // given
        CreatePostCommentRequest request = new CreatePostCommentRequest("내용", post.getId(), null);
        when(postService.getPost(post.getId())).thenReturn(post);
        when(s3Service.generatePresignedGetUrl(anyString())).thenReturn("https://url");

        // when
        PostCommentResponse response = postCommentService.createPostComment(request, user);

        // then
        verify(postCommentRepository).save(any(PostComment.class));
        assertThat(response).isNotNull();
        assertThat(response.profileUrl()).isEqualTo("https://url");
    }

    @Test
    @DisplayName("댓글 생성 실패 - 부모 댓글이 다른 게시글인 경우")
    void commentCreationFailureWithDifferentParent() {
        // given
        CreatePostCommentRequest request = new CreatePostCommentRequest("내용", post.getId(), 10L);

        Post otherPost = Post.builder().title("다른 게시글").content("내용").user(user).build();
        ReflectionTestUtils.setField(otherPost, "id", 999L);

        PostComment parent = PostComment.builder()
                .user(user)
                .post(otherPost)
                .content("부모 댓글")
                .build();
        ReflectionTestUtils.setField(parent, "id", 10L);

        when(postService.getPost(post.getId())).thenReturn(post);
        when(postCommentRepository.findById(10L)).thenReturn(Optional.of(parent));

        // when & then
        assertThatThrownBy(() -> postCommentService.createPostComment(request, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ExceptionCode.INVALID_PARENT_COMMENT.getMessage());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void commentUpdateSuccess() {
        // given
        when(postCommentRepository.findById(1L)).thenReturn(Optional.of(comment));

        UpdatePostCommetRequest request = new UpdatePostCommetRequest("수정된 내용");

        // when
        postCommentService.updatePostComment(user, request, 1L);

        // then
        assertThat(comment.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("댓글 수정 실패 - 댓글이 작성자가 아닌 경우")
    void commentUpdateFailureNotAuthor() {
        // given
        User otherUser = User.builder().nickname("other").build();
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        when(postCommentRepository.findById(1L)).thenReturn(Optional.of(comment));

        UpdatePostCommetRequest request = new UpdatePostCommetRequest("수정");

        // when & then
        assertThatThrownBy(() -> postCommentService.updatePostComment(otherUser, request, 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ExceptionCode.NOT_AUTHOR.getMessage());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void commentDeletionSuccess() {
        when(postCommentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when
        postCommentService.deletePostComment(user, 1L);

        // then
        assertThat(comment.isValid()).isFalse();
    }
}