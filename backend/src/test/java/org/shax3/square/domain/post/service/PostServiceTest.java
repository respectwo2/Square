package org.shax3.square.domain.post.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shax3.square.domain.post.dto.request.CreatePostRequest;
import org.shax3.square.domain.post.dto.request.UpdatePostRequest;
import org.shax3.square.domain.post.model.Post;
import org.shax3.square.domain.post.model.PostImage;
import org.shax3.square.domain.post.repository.PostRepository;
import org.shax3.square.domain.s3.service.S3Service;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.exception.CustomException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.shax3.square.exception.ExceptionCode.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService 테스트")
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private PostService postService;

    private User user;
    private Post dummyPost;

    @BeforeEach
    void setUp() {
        user = User.builder().build();
        dummyPost = spy(Post.builder().build());
    }

    @Test
    @DisplayName("이미지 정보가 없는 경우: postImageRepository.saveAll() 호출 없이 postRepository.save() 호출")
    void createPostWithoutImages_shouldSavePostWithoutPostImages() {
        // given
        CreatePostRequest request = mock(CreatePostRequest.class);
        when(request.to(user)).thenReturn(dummyPost);
        when(request.postImages()).thenReturn(null);

        // when
        postService.createPost(request, user);

        // then
        verify(dummyPost, never()).addPostImage(any(PostImage.class));
        verify(postRepository).save(dummyPost);
    }

    @Test
    @DisplayName("유효한 이미지 정보가 있는 경우: 올바른 PostImage 목록이 설정되고 postRepository.save()가 호출되어야 함")
    void createPostWithValidImages_shouldSavePostWithPostImages() {
        // given
        List<String> s3Keys = Arrays.asList("post/s3Key1", "post/s3Key2");
        CreatePostRequest request = mock(CreatePostRequest.class);
        when(request.to(user)).thenReturn(dummyPost);
        when(request.postImages()).thenReturn(s3Keys);

        // when
        postService.createPost(request, user);

        // then
        assertThat(dummyPost.getPostImages()).hasSize(s3Keys.size());
        for (int i = 0; i < s3Keys.size(); i++) {
            PostImage pi = dummyPost.getPostImages().get(i);
            assertThat(pi.getS3Key()).isEqualTo(s3Keys.get(i));
            assertThat(pi.getPost()).isEqualTo(dummyPost);
        }
        verify(postRepository).save(dummyPost);
        verify(dummyPost, never()).addPostImage(any(PostImage.class));
    }

    @Test
    @DisplayName("이미지 정보가 3개 초과인 경우: CustomException 발생 및 저장 메서드 호출 없음")
    void createPostWithTooManyImages_shouldThrowCustomException() {
        // given
        List<String> s3Keys = Arrays.asList("post/s3Key1", "post/s3Key2", "post/s3Key3", "post/s3Key4");
        CreatePostRequest request = mock(CreatePostRequest.class);
        when(request.to(user)).thenReturn(dummyPost);
        when(request.postImages()).thenReturn(s3Keys);

        // when & then
        assertThatThrownBy(() -> postService.createPost(request, user))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getCode()).isEqualTo(POST_IMAGE_LIMIT.getCode());
                });

        verify(dummyPost, never()).addPostImage(any(PostImage.class));
    }

    @Test
    @DisplayName("updatePost: 게시글이 존재하지 않으면 CustomException(POST_NOT_FOUND) 발생")
    void updatePost_postNotFound_shouldThrowCustomException() {
        Long postId = 100L;
        UpdatePostRequest updateRequest = mock(UpdatePostRequest.class);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.updatePost(user, postId, updateRequest))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getCode()).isEqualTo(POST_NOT_FOUND.getCode());
                });
    }

    @Test
    @DisplayName("updatePost: 작성자가 아닌 경우 CustomException(NOT_AUTHOR) 발생")
    void updatePost_nonAuthor_shouldThrowCustomException() {
        //given
        Long postId = 1L;
        User notAuthor = spy(User.builder().build());
        Post post = spy(Post.builder().user(notAuthor).build());
        User curUser = spy(User.builder().build());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(curUser.getId()).thenReturn(1L);
        when(notAuthor.getId()).thenReturn(2L);

        //when&then
        UpdatePostRequest updateRequest = mock(UpdatePostRequest.class);
        assertThatThrownBy(() -> postService.updatePost(curUser, postId, updateRequest))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getCode()).isEqualTo(NOT_AUTHOR.getCode());
                });
    }

    @Test
    @DisplayName("updatePost: 정상 업데이트 시 제목, 내용 업데이트 및 이미지 삭제/추가가 올바르게 처리되어야 함")
    void updatePost_validUpdate_shouldUpdatePostAndImages() {
        Long postId = 1L;
        User user = spy(User.builder().build());
        Post post = spy(Post.builder().user(user).build());
        PostImage image1 = PostImage.builder().s3Key("post/s3Key1").build();
        PostImage image2 = PostImage.builder().s3Key("post/s3Key2").build();
        post.setPostImages(new ArrayList<>(Arrays.asList(image1, image2)));

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(user.getId()).thenReturn(1L);

        UpdatePostRequest updateRequest = mock(UpdatePostRequest.class);
        when(updateRequest.deletedImages()).thenReturn(Arrays.asList("post/s3Key1"));
        when(updateRequest.addedImages()).thenReturn(Arrays.asList("post/s3Key3"));
        when(updateRequest.title()).thenReturn("Updated Title");
        when(updateRequest.content()).thenReturn("Updated Content");

        // when
        postService.updatePost(user, postId, updateRequest);

        // then
        verify(post).updatePost("Updated Title", "Updated Content");
        verify(post).removePostImage(argThat(img -> "post/s3Key1".equals(img.getS3Key())));
        verify(s3Service).deleteImage("post/s3Key1");
        verify(post).addPostImage(argThat(img -> "post/s3Key3".equals(img.getS3Key())));
    }

    @Test
    @DisplayName("updatePost: 추가 이미지 개수가 제한(3개)을 초과하면 CustomException(POST_IMAGE_LIMIT) 발생")
    void updatePost_tooManyImages_shouldThrowCustomException() {
        Long postId = 1L;
        User user = spy(User.builder().build());
        Post post = spy(Post.builder().user(user).build());
        PostImage image1 = PostImage.builder().s3Key("s3Key1").build();
        PostImage image2 = PostImage.builder().s3Key("s3Key2").build();
        post.setPostImages(Arrays.asList(image1, image2));

        when(user.getId()).thenReturn(1L);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        UpdatePostRequest updateRequest = mock(UpdatePostRequest.class);
        when(updateRequest.deletedImages()).thenReturn(Arrays.asList());
        when(updateRequest.addedImages()).thenReturn(Arrays.asList("s3Key3", "s3Key4"));

        assertThatThrownBy(() -> postService.updatePost(user, postId, updateRequest))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getCode()).isEqualTo(POST_IMAGE_LIMIT.getCode());
                });

        verify(s3Service, never()).deleteImage(anyString());
    }

    @Test
    @DisplayName("deletePost: 게시글이 존재하지 않으면 CustomException(POST_NOT_FOUND) 발생")
    void deletePost_postNotFound_shouldThrowCustomException() {
        Long postId = 100L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(user, postId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getCode()).isEqualTo(POST_NOT_FOUND.getCode());
                });
    }

    @Test
    @DisplayName("deletePost: 작성자가 아닌 경우 CustomException(NOT_AUTHOR) 발생")
    void deletePost_nonAuthor_shouldThrowCustomException() {
        Long postId = 1L;
        User nonAuthor = spy(User.builder().build());
        User curUser = spy(User.builder().build());
        // spy 대신 실제 객체를 사용하여 nonAuthor로 생성
        Post post = Post.builder().user(nonAuthor).build();
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(nonAuthor.getId()).thenReturn(2L);
        when(curUser.getId()).thenReturn(1L);

        assertThatThrownBy(() -> postService.deletePost(curUser, postId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException customEx = (CustomException) ex;
                    assertThat(customEx.getCode()).isEqualTo(NOT_AUTHOR.getCode());
                });
    }

    @Test
    @DisplayName("deletePost: 정상 삭제 시 이미지 삭제 및 softDelete가 올바르게 처리되어야 함")
    void deletePost_validDeletion_shouldDeleteImagesAndSoftDeletePost() {
        Long postId = 1L;
        User user = spy(User.builder().build());
        Post post = spy(Post.builder().user(user).build());
        PostImage image1 = PostImage.builder().s3Key("s3Key1").build();
        PostImage image2 = PostImage.builder().s3Key("s3Key2").build();
        post.setPostImages(new ArrayList<>(Arrays.asList(image1, image2)));

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(user.getId()).thenReturn(1L);
        when(post.getUser().getId()).thenReturn(1L);

        // when
        postService.deletePost(user, postId);

        // then
        verify(s3Service).deleteImage("s3Key1");
        verify(s3Service).deleteImage("s3Key2");
        verify(post, times(2)).removePostImage(any(PostImage.class));
        verify(post).softDelete();
    }
}
