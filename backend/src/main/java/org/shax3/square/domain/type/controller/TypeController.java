package org.shax3.square.domain.type.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.auth.annotation.AuthUser;
import org.shax3.square.domain.type.dto.request.EndTypeTestRequest;
import org.shax3.square.domain.type.dto.response.TypeInfoResponse;
import org.shax3.square.domain.type.dto.response.TypeTestQuestionResponse;
import org.shax3.square.domain.type.service.TypeService;
import org.shax3.square.domain.user.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/type")
@RequiredArgsConstructor
@Tag(name = "Type", description = "성향테스트 관련 API")
public class TypeController {

    private final TypeService typeService;

    @Operation(
            summary = "성향테스트 질문 목록 조회 api",
            description = "성향테스트 질문 목록의 순서를 랜덤으로 가져와 반환합니다."
    )
    @GetMapping
    public ResponseEntity<TypeTestQuestionResponse> getTypeTestQuestions() {
        TypeTestQuestionResponse typeTestQuestionResponse = typeService.getShuffledQuestionList();

        return ResponseEntity.ok(typeTestQuestionResponse);
    }

    @Operation(
            summary = "성향테스트 완료(생성) api",
            description = "성향테스트 답변을 입력하면 결과를 반환합니다."
    )
    @PostMapping
    public ResponseEntity<TypeInfoResponse> endTypeTest(
            @AuthUser User user,
            @Valid @RequestBody EndTypeTestRequest endTypeTestRequest
    ) {
        TypeInfoResponse typeInfoResponse = typeService.endTypeTest(user, endTypeTestRequest);

        return ResponseEntity.ok(typeInfoResponse);
    }

    @Operation(
            summary = "내 성향테스트 결과 조회 api",
            description = "토큰만 있으면 내 성향테스트 결과를 반환합니다."
    )
    @GetMapping("/my")
    public ResponseEntity<TypeInfoResponse> getMyTypeTestResult(
            @AuthUser User user
    ) {
        TypeInfoResponse typeInfoResponse = typeService.getMyTypeInfo(user);

        return ResponseEntity.ok(typeInfoResponse);
    }

    @Operation(
            summary = "타인의 성향테스트 결과 조회 api",
            description = "타인의 닉네임을 입력하면 해당 유저의 성향테스트 결과를 반환합니다."
    )
    @GetMapping("/other")
    public ResponseEntity<TypeInfoResponse> getMyTypeTestResult(
            @RequestParam String nickname
    ) {
        TypeInfoResponse typeInfoResponse = typeService.getTypeInfo(nickname);

        return ResponseEntity.ok(typeInfoResponse);
    }
}
