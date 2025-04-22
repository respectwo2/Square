package org.shax3.square.domain.debate.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.debate.dto.response.MyScrapedDebatesResponse;
import org.shax3.square.domain.debate.dto.response.MyVotedDebatesResponse;
import org.shax3.square.domain.debate.dto.response.VoteResponse;
import org.shax3.square.domain.debate.model.Debate;
import org.shax3.square.domain.debate.model.Vote;
import org.shax3.square.domain.scrap.model.Scrap;
import org.shax3.square.domain.scrap.service.ScrapService;
import org.shax3.square.domain.user.model.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebateFacadeServiceTest {

    @Mock
    private ScrapService scrapService;
    @Mock
    private DebateService debateService;
    @Mock
    private VoteService voteService;

    @InjectMocks
    private DebateFacadeService debateFacadeService;

    @Test
    @DisplayName("스크랩한 토론 목록 조회 테스트")
    void testGetScrapedDebates() {
        User user = User.builder().build();
        Debate debate = Debate.builder().topic("테스트").build();
        Vote vote = Vote.builder().debate(debate).user(user).left(true).build();
        Scrap scrap = Scrap.builder().targetId(1L).build();
        ReflectionTestUtils.setField(scrap, "id", 10L);

        when(scrapService.getPaginatedScraps(user, TargetType.DEBATE, null, 3))
                .thenReturn(List.of(scrap));
        when(debateService.findDebateById(1L)).thenReturn(debate);
        when(voteService.getVoteByUserAndDebate(user, debate)).thenReturn(Optional.of(vote));
        when(voteService.calculateVoteResult(debate)).thenReturn(VoteResponse.of(1, 0));

        MyScrapedDebatesResponse response = debateFacadeService.getScrapedDebates(user, null, 2);

        assertThat(response.scraps()).hasSize(1);
        assertThat(response.nextCursorId()).isNull();
    }

    @Test
    @DisplayName("내가 투표한 토론 목록 조회 테스트")
    void testGetMyVotedDebates() {
        User user = User.builder().build();
        Debate debate = Debate.builder().topic("투표 토론").build();
        ReflectionTestUtils.setField(debate, "id", 100L);
        Vote vote = Vote.builder().debate(debate).user(user).left(true).build();

        when(voteService.getVotesByUser(user, null, 3)).thenReturn(List.of(vote));
        when(scrapService.getScrapIds(user, TargetType.DEBATE)).thenReturn(List.of(100L));
        when(voteService.calculateVoteResult(debate)).thenReturn(VoteResponse.of(3, 2));

        MyVotedDebatesResponse response = debateFacadeService.getMyVotedDebates(user, null, 2);

        assertThat(response.debates()).hasSize(1);
        assertThat(response.debates().get(0).isScraped()).isTrue();
    }
}
