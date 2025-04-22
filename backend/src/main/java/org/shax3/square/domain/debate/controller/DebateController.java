package org.shax3.square.domain.debate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.auth.annotation.AuthUser;
import org.shax3.square.domain.auth.annotation.Guest;
import org.shax3.square.domain.debate.dto.DebateVotedResultResponse;
import org.shax3.square.domain.debate.dto.request.DebateCreateRequest;
import org.shax3.square.domain.debate.dto.request.VoteRequest;
import org.shax3.square.domain.debate.dto.response.*;
import org.shax3.square.domain.debate.model.Debate;
import org.shax3.square.domain.debate.service.DebateFacadeService;
import org.shax3.square.domain.debate.service.DebateService;
import org.shax3.square.domain.debate.service.VoteService;
import org.shax3.square.domain.user.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/debates")
@Tag(name = "Debate", description = "논쟁 API")
@RequiredArgsConstructor
public class DebateController {
    private final DebateService debateService;
    private final VoteService voteService;
    private final DebateFacadeService debateFacadeService;

    @Operation(summary = "토론 투표 API ",
            description = "해당 토론 주제에 대해 투표를 할 수 있는 기능입니다. 인증을 필요로 합니다."
    )
    @PostMapping("/vote/{debateId}")
    public ResponseEntity<VoteResponse> vote(@Valid @RequestBody VoteRequest request, @AuthUser User user, @PathVariable Long debateId) {
        Debate debate = debateService.findDebateById(debateId);
        VoteResponse response = voteService.vote(request, debate, user);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "내가 투표한 논쟁 목록 조회 API ",
            description = "사용자가 투표한 논쟁 목록을 최신순으로 조회하는 API 입니다.")
    @GetMapping("/my-votes")
    public ResponseEntity<MyVotedDebatesResponse> getMyVotedDebates(
            @RequestParam(required = false) Long nextCursorId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthUser User user) {

        MyVotedDebatesResponse response = debateFacadeService.getMyVotedDebates(user, nextCursorId, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내가 스크랩한 논쟁 목록 조회 API ",
            description = "사용자가 스크랩한 논쟁 목록을 최신순으로 조회하는 API 입니다.")
    @GetMapping("/my-scrap")
    public ResponseEntity<MyScrapedDebatesResponse> getMyScrapedDebates(
            @RequestParam(required = false) Long nextCursorId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthUser User user) {

        MyScrapedDebatesResponse response = debateFacadeService.getScrapedDebates(user, nextCursorId, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "토론에 따른 투표 통계 조회 API",
            description = "토론에 따른 투표 통계 자료를 제공합니다")
    @GetMapping("/{debateId}/result")
    public ResponseEntity<DebateVotedResultResponse> getDebateVotedResult(@AuthUser User user, @PathVariable Long debateId) {

        DebateVotedResultResponse response = debateService.getVoteResult(debateId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "AI 요약 조회 API",
            description = """
                    지정한 논쟁 ID에 해당하는 AI 요약 결과를 조회합니다. \n
                    - 로그인하지 않은 경우 `hasVoted`, `isScraped` 값은 `null`로 반환됩니다. \n
                    """
    )
    @GetMapping("/{debateId}/summary")
    public ResponseEntity<SummaryResponse> getSummary(@Guest User user, @PathVariable Long debateId) {
        SummaryResponse response = debateService.getSummaryResult(debateId, user);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "논쟁 메인 조회 API",
            description = """
                    메인 페이지에 노출될 논쟁 목록을 조회합니다. \n
                    - 스크랩, 투표 여부에 따라 `isScraped`, `isLeft` 값이 달라질 수 있습니다. \n
                    - 로그인 여부를 확인하려면 클라이언트에서 확인해야 합니다.
                    """)
    @GetMapping
    public ResponseEntity<DebatesResponse> getDebates(@Guest User user,
                                                      @RequestParam(required = false) Long nextCursorId,
                                                      @RequestParam(defaultValue = "5") int limit
    ) {
        DebatesResponse response = debateFacadeService.getDebates(user, nextCursorId, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "토론에 해당하는 의견 목록 조회 API",
            description = """
                    토론 상세 페이지에 보여질 의견(opinion) 목록을 조회합니다. \n
                    - 정렬 기준: `sort` 파라미터로 의견 정렬 방식 선택 (latest, likes, comments) \n
                    - 양 진영(left/right) 의견을 교차 병합하여 반환합니다. \n
                    - 커서 기반 페이지네이션 적용: 각 진영별로 개별 커서(`nextLeftCursorId`, `nextRightCursorId`, 등)를 이용 \n
                    - 로그인 시 각 opinion의 좋아요 여부(`isLiked`) 포함되어 응답됩니다. \n
                    - 투표 결과(`voteResult`)는 항상 응답에 포함됩니다.
                    - 하위의 커서들은 클라이언트는 그대로 반환해주시면 됩니다.
                    """
    )
    @GetMapping("/{debateId}")
    public ResponseEntity<DebateDetailResponse> getDebateDetail(
            @PathVariable Long debateId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) Long nextLeftCursorId,
            @RequestParam(required = false) Integer nextLeftCursorLikes,
            @RequestParam(required = false) Integer nextLeftCursorComments,
            @RequestParam(required = false) Long nextRightCursorId,
            @RequestParam(required = false) Integer nextRightCursorLikes,
            @RequestParam(required = false) Integer nextRightCursorComments,
            @RequestParam(defaultValue = "4") Integer limit,
            @AuthUser User user
    ) {

        DebateDetailResponse response = debateService.getDebateDetails(user, debateId, sort,
                nextLeftCursorId,
                nextLeftCursorLikes,
                nextLeftCursorComments,
                nextRightCursorId,
                nextRightCursorLikes,
                nextRightCursorComments,
                limit);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "오늘의 논쟁 생성 API ",
            description = "청원 목록 중 하나를 선택하여 오늘의 논쟁을 생성합니다."
    )
    @PostMapping("/today")
    public ResponseEntity<Void> createDebate(@Valid @RequestBody DebateCreateRequest request, @AuthUser User user) {
        debateService.createDebateFromProposal(request, user);
        return ResponseEntity.ok().build();
    }

}
