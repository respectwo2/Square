package org.shax3.square.domain.post.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.like.service.LikeService;
import org.shax3.square.domain.post.dto.response.MyCommentListResponse;
import org.shax3.square.domain.post.dto.response.RepliesResponse;
import org.shax3.square.domain.post.model.Post;
import org.shax3.square.domain.post.model.PostComment;
import org.shax3.square.domain.s3.service.S3Service;
import org.shax3.square.domain.user.model.Type;
import org.shax3.square.domain.user.model.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentFacadeServiceTest {

    @Mock
    private
    PostCommentService commentService;

    @Mock
    private LikeService likeService;

    @Mock
    private S3Service s3Service;

    @Mock
    private PostService postService;

    @Mock
    private RedisTemplate<String, String> batchRedisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private CommentFacadeService commentFacadeService;

    private User user;
    private PostComment comment1;
    private PostComment comment2;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .nickname("test-user")
                .s3Key("user/profile.jpg")
                .type(Type.PNTB)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        Post post = Post.builder().title("게시글 제목").content("내용").user(user).build();
        ReflectionTestUtils.setField(post, "id", 100L);

        comment1 = PostComment.builder().user(user).post(post).content("댓글 1").build();
        comment2 = PostComment.builder().user(user).post(post).content("댓글 2").build();
        ReflectionTestUtils.setField(comment1, "id", 10L);
        ReflectionTestUtils.setField(comment2, "id", 9L);
    }

    @Test
    @DisplayName("대댓글 더보기 조회 성공")
    void getRepliesSuccess() {
        // given
        PostComment reply1 = PostComment.builder().user(user).parent(comment1).content("대댓글 1").build();
        PostComment reply2 = PostComment.builder().user(user).parent(comment2).content("대댓글 2").build();
        ReflectionTestUtils.setField(reply1, "id", 20L);
        ReflectionTestUtils.setField(reply2, "id", 19L);

        List<PostComment> replies = List.of(reply1, reply2);
        when(commentService.getReplies(3L, null, 5)).thenReturn(replies);
        when(likeService.getLikedTargetIds(eq(user), eq(TargetType.POST_COMMENT), anyList()))
                .thenReturn(Set.of(20L));
        when(s3Service.generatePresignedGetUrl(anyString()))
                .thenReturn("https://s3.com/profile.jpg");

        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());

        // when
        RepliesResponse result = commentFacadeService.getReplies(user, 3L, null, 5);

        // then
        assertThat(result.replies()).hasSize(2);
        assertThat(result.replies()).extracting("replyId").containsExactly(20L, 19L);
        assertThat(result.replies().get(0).isLiked()).isTrue();
        assertThat(result.replies().get(1).isLiked()).isFalse();
        assertThat(result.nextCursorId()).isNull();
    }

    @Test
    @DisplayName("내가 작성한 댓글 조회 성공")
    void getMyCommentsSuccess() {
        // given
        List<PostComment> comments = List.of(comment1, comment2);
        when(commentService.getMyComments(user, null, 5)).thenReturn(comments);
        when(likeService.getLikedTargetIds(eq(user), eq(TargetType.POST_COMMENT), anyList()))
                .thenReturn(Set.of(9L));

        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());

        // when
        MyCommentListResponse result = commentFacadeService.getMyComments(user, null, 5);

        // then
        assertThat(result.comments()).hasSize(2);
        assertThat(result.comments().get(0).commentId()).isEqualTo(10L);
        assertThat(result.comments().get(1).commentId()).isEqualTo(9L);
        assertThat(result.comments().get(0).isLiked()).isFalse();
        assertThat(result.comments().get(1).isLiked()).isTrue();
        assertThat(result.nextCursorId()).isNull();
    }
}