package org.shax3.square.domain.debate.service;

import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.debate.dto.SummaryDto;
import org.shax3.square.domain.debate.model.Debate;
import org.shax3.square.domain.debate.model.Summary;
import org.shax3.square.domain.debate.repository.SummaryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SummaryService {
    private final SummaryRepository summaryRepository;

    public List<SummaryDto> getSummariesByDebateId(Long debateId) {
        List<Summary> summaries = summaryRepository.findByDebateId(debateId);
        return SummaryDto.of(summaries);
    }

    public void save(Debate debate, boolean isLeft, String content) {
        Summary summary = Summary.builder()
                .left(isLeft)
                .content(content)
                .debate(debate)
                .build();
        summaryRepository.save(summary);
    }

    public void saveAll(Debate debate, List<String> leftSummaries, List<String> rightSummaries) {
        List<Summary> summaries = new ArrayList<>();

        leftSummaries.forEach(content ->
                summaries.add(Summary.builder()
                        .debate(debate)
                        .left(true)
                        .content(content)
                        .build()));

        rightSummaries.forEach(content ->
                summaries.add(Summary.builder()
                        .debate(debate)
                        .left(false)
                        .content(content)
                        .build()));

        summaryRepository.saveAll(summaries);
    }

}
