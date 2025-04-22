package org.shax3.square.domain.proposal.service;

import lombok.RequiredArgsConstructor;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.like.service.LikeService;
import org.shax3.square.domain.proposal.dto.ProposalDto;
import org.shax3.square.domain.proposal.dto.response.ProposalsResponse;
import org.shax3.square.domain.proposal.model.Proposal;
import org.shax3.square.domain.user.model.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProposalFacadeService {

    private final LikeService likeService;
    private final RedisTemplate<String, Object> batchRedisTemplate;
    private final ProposalService proposalService;

    @Transactional(readOnly = true)
    public ProposalsResponse getProposals(User user, String sort, Long nextCursorId, Integer nextCursorLikes, int limit) {
        List<Proposal> proposals = proposalService.findProposalsBySort(sort, nextCursorId, nextCursorLikes, limit + 1);
        boolean hasNext = proposals.size() > limit;

        if (hasNext) {
            proposals = proposals.subList(0, limit);
        }

        Long newNextCursorId = proposals.isEmpty() ? null : proposals.get(proposals.size() - 1).getId();
        Integer newNextCursorLikes = (proposals.isEmpty() || !"likes".equals(sort))
                ? null
                : proposals.get(proposals.size() - 1).getLikeCount();

        List<Long> proposalIds = proposals.stream().map(Proposal::getId).toList();
        Set<Long> likedProposalIds = likeService.getLikedTargetIds(user, TargetType.PROPOSAL, proposalIds);

        Set<Object> entries = batchRedisTemplate.opsForSet().members("like:batch");

        List<ProposalDto> proposalDtos = proposals.stream()
                .map(proposal -> ProposalDto.from(
                        proposal,
                        likedProposalIds.contains(proposal.getId()),
                        proposal.getLikeCount() + likeService.getLikeCountInRedis(entries, proposal.getId(), TargetType.PROPOSAL)
                ))
                .toList();

        return ProposalsResponse.of(newNextCursorId, newNextCursorLikes, proposalDtos);
    }
}
