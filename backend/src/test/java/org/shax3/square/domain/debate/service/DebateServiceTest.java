package org.shax3.square.domain.debate.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.debate.dto.SummaryDto;
import org.shax3.square.domain.debate.dto.request.VoteRequest;
import org.shax3.square.domain.debate.dto.response.SummaryResponse;
import org.shax3.square.domain.debate.dto.response.VoteResponse;
import org.shax3.square.domain.debate.model.Debate;
import org.shax3.square.domain.debate.model.Vote;
import org.shax3.square.domain.debate.repository.DebateRepository;
import org.shax3.square.domain.scrap.service.ScrapFacadeService;
import org.shax3.square.domain.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class DebateServiceTest {

    @Mock
    private DebateRepository debateRepository;
    @Mock
    private VoteService voteService;
    @Mock
    private ScrapFacadeService scrapFacadeService;
    @Mock
    private SummaryService summaryService;

    @InjectMocks
    private DebateService debateService;

    @Test
    @DisplayName("AI 요약 응답을 정상 반환한다")
    void testGetSummaryResult() {
        // given
        Long debateId = 1L;
        Debate debate = Debate.builder()
                .topic("외모 논쟁")
                .leftOption("찬성")
                .rightOption("반대")
                .build();

        User user = User.builder().build();

        List<SummaryDto> summaryDtos = List.of(
                new SummaryDto(1L, "요약1", true),
                new SummaryDto(2L, "요약2", false)
        );

        VoteResponse voteResponse = new VoteResponse(10, 20, 30);

        when(debateRepository.findById(debateId)).thenReturn(Optional.of(debate));
        when(voteService.getVoteByUserAndDebate(user, debate)).thenReturn(Optional.of(mock(Vote.class)));
        when(scrapFacadeService.isScrapExist(user, debateId, TargetType.DEBATE)).thenReturn(true);
        when(voteService.calculateVoteResult(debate)).thenReturn(voteResponse);
        when(summaryService.getSummariesByDebateId(debateId)).thenReturn(summaryDtos);

        // when
        SummaryResponse response = debateService.getSummaryResult(debateId, user);

        // then
        assertThat(response.topic()).isEqualTo("외모 논쟁");
        assertThat(response.isScraped()).isTrue();
        assertThat(response.hasVoted()).isTrue();
        assertThat(response.summaries()).hasSize(2);
        assertThat(response.leftCount()).isEqualTo(10);
        assertThat(response.rightCount()).isEqualTo(20);
    }
}
