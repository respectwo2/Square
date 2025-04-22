package org.shax3.square.domain.post.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.post.dto.request.CreatePostRequest;
import org.shax3.square.domain.post.dto.request.UpdatePostRequest;
import org.shax3.square.domain.post.model.Post;
import org.shax3.square.domain.post.model.PostImage;
import org.shax3.square.domain.post.repository.PostRepository;
import org.shax3.square.domain.s3.service.S3Service;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.exception.CustomException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.shax3.square.exception.ExceptionCode.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final S3Service s3Service;

    @Transactional
    public void createPost(CreatePostRequest createPostRequest, User user) {
        Post post = createPostRequest.to(user);

        List<String> s3Keys = createPostRequest.postImages();

        if (s3Keys != null) {
            checkSize(s3Keys);
            List<PostImage> postImages = createPostRequest.postImages().stream()
                    .map(s3Key -> {
                        checkValidImage(s3Key);
                        return PostImage.builder()
                                .post(post)
                                .s3Key(s3Key)
                                .build();
                    })
                    .toList();
            post.setPostImages(postImages);
        }

        try {
            postRepository.save(post);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(DUPLICATE_IMAGE);
        }
    }

    private void checkSize(List<String> postImages) {
        if (postImages.size() > 3) {
            throw new CustomException(POST_IMAGE_LIMIT);
        }
    }

    @Transactional
    public void updatePost(User user, Long postId, @Valid UpdatePostRequest updatePostRequest) {
        Post post = getPost(postId);

        verifyAuthor(user, post);

        int currentCount = post.getPostImages().size();
        int newCount = currentCount - updatePostRequest.deletedImages().size() + updatePostRequest.addedImages().size();
        if (newCount > 3) {
            throw new CustomException(POST_IMAGE_LIMIT);
        }

        post.updatePost(updatePostRequest.title(), updatePostRequest.content());

        List<PostImage> imagesToRemove = post.getPostImages().stream()
                .filter(image -> updatePostRequest.deletedImages().contains(image.getS3Key()))
                .toList();
        for (PostImage image : imagesToRemove) {
            if (!post.removePostImage(image)) {
                throw new CustomException(IMAGE_NOT_FOUND);
            }
            s3Service.deleteImage(image.getS3Key());
        }

        updatePostRequest.addedImages().forEach(s3Key -> {
            checkValidImage(s3Key);
            PostImage newImage = PostImage.builder()
                    .s3Key(s3Key)
                    .build();
            post.addPostImage(newImage);
        });
    }

    private void verifyAuthor(User user, Post post) {
        if (!Objects.equals(post.getUser().getId(), user.getId())) {
            throw new CustomException(NOT_AUTHOR);
        }
    }

    private void checkValidImage(String s3Key) {
        if (!s3Key.startsWith("post/")) {
            throw new CustomException(INVALID_S3_KEY);
        }
    }

    @Transactional
    public void deletePost(User user, Long postId) {
        Post post = getPost(postId);

        verifyAuthor(user, post);

        for (PostImage image : new ArrayList<>(post.getPostImages())) {
            if (!post.removePostImage(image)) {
                throw new CustomException(IMAGE_NOT_FOUND);
            }
            s3Service.deleteImage(image.getS3Key());
        }
        post.softDelete();
    }

    public void validateExists(Long id) {
        if (!postRepository.existsById(id)) {
            throw new CustomException(POST_NOT_FOUND);
        }
    }

    public Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
    }

    public void increaseLikeCount(Long targetId, int countDiff) {
        Post post = getPost(targetId);
        post.increaseLikeCount(countDiff);
    }

}
