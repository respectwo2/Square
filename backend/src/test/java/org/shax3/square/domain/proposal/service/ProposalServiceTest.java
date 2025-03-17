package org.shax3.square.domain.proposal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shax3.square.domain.proposal.dto.request.CreateProposalRequest;
import org.shax3.square.domain.proposal.dto.response.CreateProposalsResponse;
import org.shax3.square.domain.proposal.dto.response.ProposalsResponse;
import org.shax3.square.domain.proposal.model.Proposal;
import org.shax3.square.domain.proposal.repository.ProposalRepository;
import org.shax3.square.domain.user.model.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProposalServiceTest {

    @Mock
    private ProposalRepository proposalRepository;

    @InjectMocks
    private ProposalService proposalService;

    private User mockUser;
    private Proposal mockProposal;
    private List<Proposal> mockProposals;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .nickname("TestUser")
                .build();
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        mockProposal = Proposal.builder()
                .topic("Sample Proposal")
                .user(mockUser)
                .build();
        ReflectionTestUtils.setField(mockProposal, "id", 1L);


        mockProposals = List.of(
                Proposal.builder().user(mockUser).topic("Topic 1").build(),
                Proposal.builder().user(mockUser).topic("Topic 2").build(),
                Proposal.builder().user(mockUser).topic("Topic 3").build(),
                Proposal.builder().user(mockUser).topic("Topic 4").build(),
                Proposal.builder().user(mockUser).topic("Topic 5").build()
        );

        ReflectionTestUtils.setField(mockProposals.get(0), "likeCount", 4);
        ReflectionTestUtils.setField(mockProposals.get(1), "likeCount", 3);
        ReflectionTestUtils.setField(mockProposals.get(2), "likeCount", 5);
        ReflectionTestUtils.setField(mockProposals.get(3), "likeCount", 2);
        ReflectionTestUtils.setField(mockProposals.get(4), "likeCount", 1);
    }

    @Test
    @DisplayName("청원 저장 성공 테스트")
    void save_success() {
        CreateProposalRequest request = new CreateProposalRequest("Sample Proposal");
        CreateProposalsResponse response = proposalService.save(request, "mock-token");

        assertThat(response).isNotNull();
        verify(proposalRepository, times(1)).save(any(Proposal.class));
    }


    @Test
    @DisplayName("최신순 목록 조회 테스트")
    void getProposals_sort_latest(){

        when(proposalRepository.findProposalsByLatest(null, 3))
                .thenReturn(mockProposals);

        ProposalsResponse response = proposalService.getProposals("latest", null, null, 3);

        assertThat(response.getProposals()).hasSize(3);
        assertThat(response.getNextCursorId()).isEqualTo(mockProposals.get(2).getId());

    }

    @Test
    @DisplayName("좋아요순 목록 조회 테스트")
    public void getProposals_sort_likeCount(){
        List<Proposal> expectedSortedLikesList = List.of(
                mockProposals.get(2), //likecount 5
                mockProposals.get(0), //likecount 4
                mockProposals.get(1) //likecount 3
        );

        when(proposalRepository.findProposalsByLikes(null, null, 3))
                .thenReturn(expectedSortedLikesList);

        ProposalsResponse response = proposalService.getProposals("likes", null, null, 3);

        assertThat(response.getProposals()).hasSize(3);
        assertThat(response.getProposals().get(0).getLikeCount()).isEqualTo(5);
        assertThat(response.getProposals().get(1).getLikeCount()).isEqualTo(4);
        assertThat(response.getProposals().get(2).getLikeCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("좋아요순 목록 조회 - 목록이 부족해 limit보다 결과 size가 작은 경우")
    public void getProposals_sort_likeCount_withCursor(){
        List<Proposal> expectedSortedLikesList = List.of(
                mockProposals.get(2), //likecount 5
                mockProposals.get(0) //likecount 4
        );

        when(proposalRepository.findProposalsByLikes(null, 3, 3))
                .thenReturn(expectedSortedLikesList);

        ProposalsResponse response = proposalService.getProposals("likes", null, 3, 3);

        assertThat(response.getProposals()).hasSize(2);
        assertThat(response.getProposals().get(0).getLikeCount()).isEqualTo(5);
        assertThat(response.getProposals().get(1).getLikeCount()).isEqualTo(4);
    }

}