package org.shax3.square.domain.debate.service;

import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.debate.dto.DebateVotedResultResponse;
import org.shax3.square.domain.debate.dto.SummaryDto;
import org.shax3.square.domain.debate.dto.VoteResultDto;
import org.shax3.square.domain.debate.dto.response.DebateDetailResponse;
import org.shax3.square.domain.debate.dto.response.SummaryResponse;
import org.shax3.square.domain.debate.dto.response.VoteResponse;
import org.shax3.square.domain.debate.model.Debate;
import org.shax3.square.domain.debate.model.Vote;
import org.shax3.square.domain.debate.repository.DebateRepository;
import org.shax3.square.domain.opinion.dto.OpinionDto;
import org.shax3.square.domain.opinion.service.OpinionFacadeService;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.shax3.square.exception.ExceptionCode.DEBATE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DebateService {

    private final DebateRepository debateRepository;
    private final VoteService voteService;
    private final SummaryService summaryService;
    private final OpinionFacadeService opinionFacadeService;

    public Debate findDebateById(Long debateId) {
        return debateRepository.findById(debateId)
                .orElseThrow(() -> new CustomException(DEBATE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public DebateVotedResultResponse getVoteResult(Long debateId) {
        Debate debate = findDebateById(debateId);
        List<Vote> votes = voteService.getVotesByDebate(debate);

        Map<Boolean, List<Vote>> groupedVotes = votes.stream()
                .collect(Collectors.partitioningBy(Vote::isLeft));

        return new DebateVotedResultResponse(
                createVoteResultDto(groupedVotes.get(true)),
                createVoteResultDto(groupedVotes.get(false))
        );
    }

    //TODO 레디스처리
    public SummaryResponse getSummaryResult(Long debateId, User user) {
        Debate debate = findDebateById(debateId);

        VoteResponse voteResponse = null;

        if (user != null) {
            voteResponse = voteService.calculateVoteResult(debate);
        }

        List<SummaryDto> summaries = summaryService.getSummariesByDebateId(debateId);
        return SummaryResponse.of(voteResponse, summaries);
    }

    public List<Debate> findMainDebatesForCursor(Long nextCursorId, int limit) {
        return debateRepository.findDebatesForMain(nextCursorId, limit);
    }

    private VoteResultDto createVoteResultDto(List<Vote> votes) {
        return VoteResultDto.fromVotes(votes);
    }


    @Transactional(readOnly = true)
    public DebateDetailResponse getDebateDetails(
            User user,
            Long debateId,
            String sort,
            Long nextLeftCursorId,
            Integer nextLeftCursorLikes,
            Integer nextLeftCursorComments,
            Long nextRightCursorId,
            Integer nextRightCursorLikes,
            Integer nextRightCursorComments,
            int limit
    ) {
        Debate debate = findDebateById(debateId);
        VoteResponse voteResponse = voteService.calculateVoteResult(debate);

        boolean firstRequest = (nextLeftCursorId == null && nextLeftCursorLikes == null && nextLeftCursorComments == null &&
                nextRightCursorId == null && nextRightCursorLikes == null && nextRightCursorComments == null);

        if (user != null && firstRequest) {
            voteResponse = voteService.calculateVoteResult(debate);
        }

        // 각 진영에서 최대 limit * 2개씩 불러와서 교차 병합
        List<OpinionDto> leftOpinions = opinionFacadeService.getOpinionsBySort(
                user, debateId, true, sort,
                nextLeftCursorId, nextLeftCursorLikes, nextLeftCursorComments, limit * 2
        );

        List<OpinionDto> rightOpinions = opinionFacadeService.getOpinionsBySort(
                user, debateId, false, sort,
                nextRightCursorId, nextRightCursorLikes, nextRightCursorComments, limit * 2
        );

        List<OpinionDto> merged = mergeAlternating(leftOpinions, rightOpinions, limit);

        // 새로운 커서 계산 (병합된 결과에서 각 진영 마지막으로 사용된 opinion 기준)
        Long nextLeftId = null, nextRightId = null;
        Integer nextLeftLikes = null, nextRightLikes = null;
        Integer nextLeftComments = null, nextRightComments = null;

        for (int i = merged.size() - 1; i >= 0; i--) {
            OpinionDto o = merged.get(i);
            if (o.isLeft() && nextLeftId == null) {
                nextLeftId = o.opinionId();
                if (sort.equals("likes")) nextLeftLikes = o.likeCount();
                if (sort.equals("comments")) nextLeftComments = o.commentCount();
            } else if (!o.isLeft() && nextRightId == null) {
                nextRightId = o.opinionId();
                if (sort.equals("likes")) nextRightLikes = o.likeCount();
                if (sort.equals("comments")) nextRightComments = o.commentCount();
            }
            if (nextLeftId != null && nextRightId != null) break;
        }

        return DebateDetailResponse.of(
                debate,
                merged,
                nextLeftId, nextLeftLikes, nextLeftComments,
                nextRightId, nextRightLikes, nextRightComments,
                voteResponse
        );
    }


    private List<OpinionDto> mergeAlternating(List<OpinionDto> left, List<OpinionDto> right, int limit) {
        List<OpinionDto> merged = new ArrayList<>();
        Iterator<OpinionDto> l = left.iterator();
        Iterator<OpinionDto> r = right.iterator();

        while (merged.size() < limit && (l.hasNext() || r.hasNext())) {
            if (l.hasNext()) merged.add(l.next());
            if (merged.size() >= limit) break;

            if (r.hasNext()) merged.add(r.next());
        }

        return merged;
    }
}