package org.shax3.square.domain.debate.service;

import lombok.RequiredArgsConstructor;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.debate.dto.DebateDto;
import org.shax3.square.domain.debate.dto.request.VoteRequest;
import org.shax3.square.domain.debate.dto.response.MyScrapedDebatesResponse;
import org.shax3.square.domain.debate.dto.response.MyVotedDebatesResponse;
import org.shax3.square.domain.debate.dto.response.VoteResponse;
import org.shax3.square.domain.debate.model.Debate;
import org.shax3.square.domain.debate.model.Vote;
import org.shax3.square.domain.debate.repository.VoteRepository;
import org.shax3.square.domain.scrap.model.Scrap;
import org.shax3.square.domain.scrap.service.ScrapService;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.shax3.square.exception.ExceptionCode.*;


@Service
@RequiredArgsConstructor
public class VoteService {
    private final VoteRepository voteRepository;
    private final DebateService debateService;
    private final ScrapService scrapService;

    @Transactional
    public VoteResponse vote(VoteRequest request, Long debateId, User user) {
        Debate debate = debateService.findDebateById(debateId);

        if (voteRepository.existsByDebateAndUser(debate, user)) {
            throw new CustomException(ALREADY_VOTED);
        }

        Vote vote = request.to(debate, user);
        voteRepository.save(vote);

        return calculateVoteResult(debate);
    }

    public VoteResponse calculateVoteResult(Debate debate) {
        int total = voteRepository.countByDebate(debate);
        int left = voteRepository.countByDebateAndLeftTrue(debate);
        int right = total - left;
        return VoteResponse.of(left, right, total);
    }

    @Transactional(readOnly = true)
    public MyVotedDebatesResponse getMyVotedDebates(User user, Long nextCursorId, int limit) {
        List<Vote> votes = voteRepository.findByUserOrderByIdDesc(user, nextCursorId, limit + 1);
        //다음 페이지가 있는지 (repository에서 +1 하기 때문에 다음 페이지가 있다면 size가 더 커야함
        boolean hasNext = votes.size() > limit;
        //다음 페이지가 있다면, 하나를 자름
        List<Vote> pageVotes = hasNext ? votes.subList(0, limit) : votes;

        List<Long> userScraps = scrapService.getDebateScrap(user);

        List<DebateDto> debates = pageVotes.stream()
                .map(vote -> {
                    Debate debate = vote.getDebate();
                    boolean isScraped = userScraps.contains(debate.getId());
                    VoteResponse voteResponse = calculateVoteResult(debate);
                    return DebateDto.of(vote,voteResponse,isScraped);
                })
                .toList();

        Long newNextCursorId = hasNext && !pageVotes.isEmpty() ? pageVotes.get(pageVotes.size() - 1).getId() : null;

        return new MyVotedDebatesResponse(debates, newNextCursorId);
    }

    @Transactional(readOnly = true)
    public MyScrapedDebatesResponse getScrapedDebates(User user, Long nextCursorId, int limit) {
        List<Scrap> scraps = scrapService.getPaginatedScraps(user, TargetType.DEBATE, nextCursorId, limit + 1);

        boolean hasNext = scraps.size() > limit;
        List<Scrap> pageScraps = hasNext ? scraps.subList(0, limit) : scraps;
        List<DebateDto> debates = pageScraps.stream()
                .map(scrap -> {
                    //TODO debateService 대신 Redis에서 가져오는걸로 변경
                    Debate debate = debateService.findDebateById(scrap.getTargetId());
                    Optional<Vote> userVote = voteRepository.findByDebateAndUser(debate,user);
                    Boolean isLeft = userVote.map(Vote::isLeft).orElse(null);

                    VoteResponse voteResponse = (isLeft != null)
                            ? calculateVoteResult(debate)
                            : VoteResponse.create();

                    return DebateDto.of(debate, isLeft, voteResponse, true);
                })
                .toList();

        Long newNextCursorId = hasNext && !pageScraps.isEmpty() ? pageScraps.get(pageScraps.size() - 1).getId() : null;

        return new MyScrapedDebatesResponse(debates, newNextCursorId);
    }

}


