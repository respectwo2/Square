package org.shax3.square.domain.like.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.auth.annotation.AuthUser;
import org.shax3.square.domain.like.dto.LikeRequest;
import org.shax3.square.domain.like.service.LikeService;
import org.shax3.square.domain.user.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Like", description = "좋아요 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/likes")
public class LikeController {

    private final LikeService likeService;

    @Operation(
            summary = "좋아요 api",
            description = "사용자가 특정 대상에 좋아요를 생성 or 취소합니다."
    )
    @PostMapping
    public ResponseEntity<Void> like(
            @AuthUser User user,
            @Valid @RequestBody LikeRequest likeRequest
    ) {
        likeService.like(user, likeRequest);
        return ResponseEntity.ok().build();
    }
}
