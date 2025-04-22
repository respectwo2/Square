package org.shax3.square.domain.proposal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.auth.annotation.AuthUser;
import org.shax3.square.domain.proposal.dto.request.CreateProposalRequest;
import org.shax3.square.domain.proposal.dto.response.CreateProposalsResponse;
import org.shax3.square.domain.proposal.dto.response.ProposalsResponse;
import org.shax3.square.domain.proposal.service.ProposalFacadeService;
import org.shax3.square.domain.proposal.service.ProposalService;
import org.shax3.square.domain.user.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Proposal", description = "주제 청원 API")
@RestController
@RequestMapping("/proposals")
@RequiredArgsConstructor
public class ProposalController {
    private final ProposalService proposalService;
    private final ProposalFacadeService proposalFacadeService;

    @Operation(
            summary = "논쟁 주제 제안",
            description = "사용자가 새로운 논쟁 주제를 제안하는 API입니다. 인증을 필요로 합니다."
    )
    @PostMapping
    public ResponseEntity<CreateProposalsResponse> createProposal(@Valid @RequestBody CreateProposalRequest request,
                                                                  @AuthUser User user) {
        CreateProposalsResponse response = proposalService.save(request, user);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "청원 목록 조회",
            description = "청원에 등록된 주제를 최신순 또는 인기순으로 조회하는 API입니다. 커서 기반 페이지네이션을 지원합니다."
    )
    @GetMapping
    public ResponseEntity<ProposalsResponse> getProposals(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) Long nextCursorId,
            @RequestParam(required = false) Integer nextCursorLikes,
            @RequestParam(defaultValue = "5") int limit,
            @AuthUser User user
    ) {
        ProposalsResponse response = proposalFacadeService.getProposals(user, sort, nextCursorId, nextCursorLikes, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "청원 삭제",
            description = "등록된 청원을 삭제하는 API입니다. 인증을 필요로 합니다."

    )
    @DeleteMapping("/{proposalId}")
    public ResponseEntity<Void> deleteProposal(@PathVariable Long proposalId) {
        proposalService.deleteProposal(proposalId);
        return ResponseEntity.ok().build();
    }

}
