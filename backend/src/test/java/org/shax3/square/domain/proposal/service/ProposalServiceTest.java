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
import org.shax3.square.domain.proposal.model.Proposal;
import org.shax3.square.domain.proposal.repository.ProposalRepository;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.exception.CustomException;
import org.shax3.square.exception.ExceptionCode;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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

        ReflectionTestUtils.setField(mockProposals.get(0), "id", 1L);
        ReflectionTestUtils.setField(mockProposals.get(1), "id", 2L);
        ReflectionTestUtils.setField(mockProposals.get(2), "id", 3L);
        ReflectionTestUtils.setField(mockProposals.get(3), "id", 4L);
        ReflectionTestUtils.setField(mockProposals.get(4), "id", 5L);

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
        CreateProposalsResponse response = proposalService.save(request, mockUser);

        assertThat(response).isNotNull();
        verify(proposalRepository, times(1)).save(any(Proposal.class));
    }


    @Test
    @DisplayName("소프트 딜리트 테스트 - 한번만 호출되는지 검증")
    void softDeleteProposal() {
        Proposal proposal = Proposal.builder()
                .user(mockUser)
                .topic("Test Proposal")
                .build();
        ReflectionTestUtils.setField(proposal, "id", 1L);

        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));

        proposalService.deleteProposal(1L);

        assertThat(proposal.isValid()).isFalse();
        verify(proposalRepository, times(1)).findById(1L);

    }

    @Test
    @DisplayName("청원이 존재하는 경우 true 반환")
    void isOpinionExists_whenPresent() {
        // given
        Long proposalId = 3L;
        when(proposalRepository.existsById(proposalId)).thenReturn(true);

        // when & then
        assertThatCode(() -> proposalService.validateExists(proposalId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("청원이 존재하지 않는 경우 false 반환")
    void isOpinionExists_whenNotPresent() {
        // given
        Long proposalId = 4L;
        when(proposalRepository.existsById(proposalId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> proposalService.validateExists(proposalId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.PROPOSAL_NOT_FOUND.getMessage());
    }


    @Test
    @DisplayName("getProposal - 존재하는 proposal이면 반환")
    void getProposal_success() {
        // given
        Proposal proposal = Proposal.builder()
                .user(mockUser)
                .topic("Test Proposal")
                .build();
        when(proposalRepository.findById(10L)).thenReturn(Optional.of(proposal));

        // when
        Proposal result = proposalService.getProposal(10L);

        // then
        assertThat(result).isEqualTo(proposal);
    }


    @Test
    @DisplayName("getProposal - 존재하지 않으면 예외 발생")
    void getProposal_notFound() {
        // given
        when(proposalRepository.findById(10L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> proposalService.getProposal(10L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.PROPOSAL_NOT_FOUND.getMessage());
    }


    @Test
    @DisplayName("increaseLikeCount - likeCount 증가 로직이 잘 호출되는지 확인")
    void increaseLikeCount_success() {
        // given
        Proposal proposal = mock(Proposal.class);
        when(proposalRepository.findById(10L)).thenReturn(Optional.of(proposal));

        // when
        proposalService.increaseLikeCount(10L, 3);

        // then
        verify(proposal).increaseLikeCount(3);
    }
}

