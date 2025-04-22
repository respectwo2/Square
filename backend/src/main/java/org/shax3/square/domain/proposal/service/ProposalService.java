package org.shax3.square.domain.proposal.service;

import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.proposal.dto.request.CreateProposalRequest;
import org.shax3.square.domain.proposal.dto.response.CreateProposalsResponse;
import org.shax3.square.domain.proposal.model.Proposal;
import org.shax3.square.domain.proposal.repository.ProposalRepository;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.shax3.square.exception.ExceptionCode.ALREADY_DELETED;
import static org.shax3.square.exception.ExceptionCode.PROPOSAL_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProposalService {
    private final ProposalRepository proposalRepository;

    @Transactional
    public CreateProposalsResponse save(CreateProposalRequest request, User user) {

        Proposal proposal = request.to(user);
        proposalRepository.save(proposal);

        return CreateProposalsResponse.of(proposal.getId());
    }

    public List<Proposal> findProposalsBySort(String sort, Long nextCursorId, Integer nextCursorLikes, int limit) {
        if ("likes".equals(sort)) {
            return proposalRepository.findProposalsByLikes(nextCursorId, nextCursorLikes, limit);
        }
        return proposalRepository.findProposalsByLatest(nextCursorId, limit);
    }

    @Transactional
    public void deleteProposal(Long id) {
        Proposal proposal = getProposal(id);
        if (!proposal.isValid()) {
            throw new CustomException(ALREADY_DELETED);
        }
        proposal.softDelete();
    }

    public Proposal getProposal(Long proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new CustomException(PROPOSAL_NOT_FOUND));
    }

    public void validateExists(Long proposalId) {
        if (!proposalRepository.existsById(proposalId)) {
            throw new CustomException(PROPOSAL_NOT_FOUND);
        }
    }

    public void increaseLikeCount(Long targetId, int countDiff) {
        Proposal proposal = getProposal(targetId);
        proposal.increaseLikeCount(countDiff);
    }
}



