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
import org.shax3.square.domain.post.dto.response.MyPostResponse;
import org.shax3.square.domain.post.dto.response.PostDetailResponse;
import org.shax3.square.domain.post.dto.response.PostListResponse;
import org.shax3.square.domain.post.model.Post;
import org.shax3.square.domain.post.model.PostComment;
import org.shax3.square.domain.post.model.PostImage;
import org.shax3.square.domain.s3.service.S3Service;
import org.shax3.square.domain.scrap.service.ScrapService;
import org.shax3.square.domain.user.model.Type;
import org.shax3.square.domain.user.model.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostFacadeServiceTest {

    @Mock
    private PostQueryService postQueryService;

    @Mock
    private PostCommentService postCommentService;

    @Mock
    private LikeService likeService;

    @Mock
    private S3Service s3Service;

    @Mock
    private PostService postService;

    @Mock
    private ScrapService scrapService;

    @Mock
    private RedisTemplate<String, String> batchRedisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private PostFacadeService postFacadeService;

    private User user;
    private List<Post> posts;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .nickname("test-user")
                .type(Type.PNTB)
                .s3Key("user/profile.jpg")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        Post post1 = Post.builder().user(user).title("title1").content("content1").build();
        Post post2 = Post.builder().user(user).title("title2").content("content2").build();
        Post post3 = Post.builder().user(user).title("title3").content("content3").build();

        ReflectionTestUtils.setField(post1, "id", 101L);
        ReflectionTestUtils.setField(post2, "id", 102L);
        ReflectionTestUtils.setField(post3, "id", 103L);
        ReflectionTestUtils.setField(post1, "likeCount", 5);
        ReflectionTestUtils.setField(post2, "likeCount", 10);
        ReflectionTestUtils.setField(post3, "likeCount", 15);

        posts = List.of(post1, post2, post3);
    }

    @DisplayName("최신순 게시글 3개 조회 성공")
    @Test
    void getPostListSortedByLatest() {
        // given
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        when(postQueryService.getPopularPosts(3)).thenReturn(List.of());
        when(postQueryService.getPostsByLatestCursor(null, 3)).thenReturn(posts);
        when(postCommentService.getCommentCounts(List.of())).thenReturn(Map.of());
        when(postCommentService.getCommentCounts(postIds)).thenReturn(Map.of(
                101L, 1,
                102L, 2,
                103L, 3
        ));
        when(likeService.getLikedTargetIds(eq(user), eq(TargetType.POST), anyList())).thenReturn(Set.of(102L));
        when(s3Service.generatePresignedGetUrl("user/profile.jpg")).thenReturn("https://s3.com/user/profile.jpg");

        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());

        // when
        PostListResponse response = postFacadeService.getPostList(user, "latest", null, null, 3);

        // then
        assertThat(response.posts()).hasSize(3);
        assertThat(response.posts()).extracting("title")
                .containsExactly("title1", "title2", "title3");
        assertThat(response.posts()).extracting("isLiked")
                .containsExactly(false, true, false);
        assertThat(response.posts()).extracting("commentCount")
                .containsExactly(1, 2, 3);
        assertThat(response.nextCursorId()).isNull();
        assertThat(response.nextCursorLikes()).isNull();
    }

    @DisplayName("좋아요순 게시글 3개 조회 성공")
    @Test
    void getPostListSortedByLikes() {
        // given
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        when(postQueryService.getPopularPosts(3)).thenReturn(List.of());
        when(postCommentService.getCommentCounts(List.of())).thenReturn(Map.of());
        when(postQueryService.getPostsByLikesCursor(null, null, 3)).thenReturn(posts);
        when(postCommentService.getCommentCounts(postIds)).thenReturn(Map.of(
                101L, 1,
                102L, 2,
                103L, 3
        ));
        when(likeService.getLikedTargetIds(eq(user), eq(TargetType.POST), anyList())).thenReturn(Set.of(101L, 103L));
        when(s3Service.generatePresignedGetUrl("user/profile.jpg")).thenReturn("https://s3.com/user/profile.jpg");

        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());

        // when
        PostListResponse response = postFacadeService.getPostList(user, "likes", null, null, 3);

        // then
        assertThat(response.posts()).hasSize(3);
        assertThat(response.posts()).extracting("title")
                .containsExactly("title1", "title2", "title3");
        assertThat(response.posts()).extracting("isLiked")
                .containsExactly(true, false, true);
        assertThat(response.posts()).extracting("commentCount")
                .containsExactly(1, 2, 3);
        assertThat(response.nextCursorId()).isNull();
        assertThat(response.nextCursorLikes()).isNull();
    }

    @DisplayName("내가 작성한 게시글 조회 성공")
    @Test
    void getMyPosts_success() {
        // given
        List<Long> postIds = posts.stream().map(Post::getId).toList();

        when(postQueryService.getMyPosts(user, null, 3)).thenReturn(posts);
        when(postCommentService.getCommentCounts(postIds)).thenReturn(Map.of(
                101L, 1,
                102L, 2,
                103L, 3
        ));
        when(likeService.getLikedTargetIds(eq(user), eq(TargetType.POST), anyList())).thenReturn(Set.of(101L));
        when(s3Service.generatePresignedGetUrl("user/profile.jpg")).thenReturn("https://s3.com/user/profile.jpg");
        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());
        // when
        MyPostResponse response = postFacadeService.getMyPostList(user, null, 3);

        // then
        assertThat(response.posts()).hasSize(3);
        assertThat(response.posts()).extracting("title")
                .containsExactly("title1", "title2", "title3");
        assertThat(response.posts()).extracting("isLiked")
                .containsExactly(true, false, false);
        assertThat(response.posts()).extracting("commentCount")
                .containsExactly(1, 2, 3);
        assertThat(response.nextCursorId()).isNull();
    }

    @DisplayName("내가 좋아요한 게시글 조회 성공")
    @Test
    void getMyLikedPosts_success() {
        // given
        List<Long> postIds = posts.stream().map(Post::getId).toList();

        when(postQueryService.getMyLikedPosts(user, null, 3)).thenReturn(posts);
        when(postCommentService.getCommentCounts(postIds)).thenReturn(Map.of(
                101L, 1,
                102L, 2,
                103L, 3
        ));
        when(likeService.getLikedTargetIds(eq(user), eq(TargetType.POST), anyList())).thenReturn(Set.of(103L));
        when(s3Service.generatePresignedGetUrl("user/profile.jpg")).thenReturn("https://s3.com/user/profile.jpg");
        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());

        // when
        MyPostResponse response = postFacadeService.getMyLikedPostList(user, null, 3);

        // then
        assertThat(response.posts()).hasSize(3);
        assertThat(response.posts()).extracting("title")
                .containsExactly("title1", "title2", "title3");
        assertThat(response.posts()).extracting("isLiked")
                .containsExactly(false, false, true);
        assertThat(response.posts()).extracting("commentCount")
                .containsExactly(1, 2, 3);
        assertThat(response.nextCursorId()).isNull();
    }

    @DisplayName("내가 스크랩한 게시글 조회 성공")
    @Test
    void getMyScrapPosts_success() {
        // given
        List<Long> postIds = posts.stream().map(Post::getId).toList();

        when(postQueryService.getMyScrapPosts(user, null, 4)).thenReturn(posts);
        when(postCommentService.getCommentCounts(postIds)).thenReturn(Map.of(
                101L, 1,
                102L, 2,
                103L, 3
        ));
        when(likeService.getLikedTargetIds(eq(user), eq(TargetType.POST), anyList())).thenReturn(Set.of(102L, 103L));
        when(s3Service.generatePresignedGetUrl("user/profile.jpg")).thenReturn("https://s3.com/user/profile.jpg");
        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());

        // when
        MyPostResponse response = postFacadeService.getMyScrapPostList(user, null, 3);

        // then
        assertThat(response.posts()).hasSize(3);
        assertThat(response.posts()).extracting("title")
                .containsExactly("title1", "title2", "title3");
        assertThat(response.posts()).extracting("isLiked")
                .containsExactly(false, true, true);
        assertThat(response.posts()).extracting("commentCount")
                .containsExactly(1, 2, 3);
        assertThat(response.nextCursorId()).isNull();
    }

    @DisplayName("게시글 상세 조회 성공")
    @Test
    void getPostDetail_success() {
        // given
        Post post = posts.get(0);
        Long postId = post.getId();

        // 이미지 설정
        PostImage image1 = PostImage.builder().post(post).s3Key("image1.jpg").build();
        PostImage image2 = PostImage.builder().post(post).s3Key("image2.jpg").build();
        ReflectionTestUtils.setField(image1, "id", 1L);
        ReflectionTestUtils.setField(image2, "id", 2L);
        post.setPostImages(List.of(image1, image2));

        PostComment comment1 = PostComment.builder().post(post).user(user).content("댓글1").build();
        PostComment comment2 = PostComment.builder().post(post).user(user).content("댓글2").build();
        ReflectionTestUtils.setField(comment1, "id", 201L);
        ReflectionTestUtils.setField(comment2, "id", 202L);

        PostComment reply1 = PostComment.builder().post(post).user(user).content("대댓글1").parent(comment1).build();
        PostComment reply2 = PostComment.builder().post(post).user(user).content("대댓글2").parent(comment1).build();
        ReflectionTestUtils.setField(reply1, "id", 301L);
        ReflectionTestUtils.setField(reply2, "id", 302L);

        when(postService.getPost(postId)).thenReturn(post);
        when(s3Service.generatePresignedGetUrl("image1.jpg")).thenReturn("https://s3.com/image1.jpg");
        when(s3Service.generatePresignedGetUrl("image2.jpg")).thenReturn("https://s3.com/image2.jpg");
        when(likeService.isTargetLiked(user, TargetType.POST, postId)).thenReturn(true);
        when(scrapService.isTargetScraped(user, postId, TargetType.POST)).thenReturn(false);
        when(postCommentService.getParentComments(post)).thenReturn(List.of(comment1, comment2));
        when(postCommentService.getReplyCounts(List.of(201L, 202L))).thenReturn(Map.of(201L, 2, 202L, 0));
        when(postCommentService.getFirstNRepliesByCommentIds(List.of(201L, 202L), 3)).thenReturn(Map.of(
                201L, List.of(reply1, reply2)
        ));
        when(likeService.getLikedTargetIds(user, TargetType.POST_COMMENT, List.of(201L, 202L))).thenReturn(Set.of(202L));
        when(likeService.getLikedTargetIds(user, TargetType.POST_COMMENT, List.of(301L, 302L))).thenReturn(Set.of(302L));
        when(s3Service.generatePresignedGetUrl("user/profile.jpg")).thenReturn("https://s3.com/user/profile.jpg");

        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());
        // when
        PostDetailResponse response = postFacadeService.getPostDetail(user, postId);

        // then
        assertThat(response.postId()).isEqualTo(postId);
        assertThat(response.images()).hasSize(2);
        assertThat(response.isLiked()).isTrue();
        assertThat(response.isScrapped()).isFalse();
        assertThat(response.commentCount()).isEqualTo(2);
        assertThat(response.comments()).hasSize(2);
        assertThat(response.comments().get(0).replies()).hasSize(2);
    }
}