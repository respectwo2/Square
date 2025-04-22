package org.shax3.square.domain.debate;

import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.debate.model.Debate;
import org.shax3.square.domain.debate.service.SummaryService;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AiSummaryClient {

    private final SummaryService summaryService;
    private final OpenAiChatClient chatClient;

    public void generateSummaries(Debate debate) {
        String topic = debate.getTopic();
        String leftOption = debate.getLeftOption();
        String rightOption = debate.getRightOption();

        String prompt = generatePrompt(topic, leftOption, rightOption);
        String result = chatClient.call(prompt);

        splitAndSaveSummaries(debate, result, leftOption, rightOption);
    }

    private void splitAndSaveSummaries(Debate debate, String result, String leftOption, String rightOption) {
        // String[] parts = result.split(leftOption + ":|"+ rightOption + ":");

        String[] parts = result.split(Pattern.quote(leftOption + ":") + "|" + Pattern.quote(rightOption + ":"));

        if (parts.length < 3) {

            List<String> defaultLeft = List.of(
                    "대중교통 무료화는 경제적으로 취약한 계층들에게 경제 부담을 덜어주며 사회적 평등을 증진시킬 수 있다",
                    "환경 보호에 기여하여 대기 오염과 교통 체증 문제를 완화시킬 수 있다.",
                    "대중교통 이용 활성화로 자동차 이용을 줄여 에너지 절약과 친환경 교통수단 이용을 촉진시킬 수 있다."
            );

            List<String> defaultRight = List.of(
                    "대중교통 무료화는 예산 부담이 커져 다른 사회복지 예산이 부족해질 우려가 있다.",
                    "서비스 품질 저하와 혼잡으로 이어져 정상 이용객들에게 불편을 줄 수 있다.",
                    "무료화로 인해 남용하는 사람들이 늘어나 정상적인 이용을 하는 시민들에게 피해를 줄 수 있다."
            );

            summaryService.saveAll(debate, defaultLeft, defaultRight);
            return;
        }

        String leftPart = parts[1].trim();
        String rightPart = parts[2].trim();

        List<String> leftSummaries = Arrays.stream(leftPart.split("/"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        List<String> rightSummaries = Arrays.stream(rightPart.split("/"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        summaryService.saveAll(debate, leftSummaries, rightSummaries);

    }

    private String generatePrompt(String topic, String leftOption, String rightOption) {
        return String.format(
                """
                        "%s" 주제에 대해 %s 측과 %s 측의 대표적인 주장 3개씩 총 6개를 추출해야돼.
                        
                        반드시 아래 형식을 그대로 따라야 해. 절대 변형하지 마!
                        
                        응답 형식:
                        %s: 주장1내용/ 주장2내용/ 주장3내용/ %s: 주장4내용/ 주장5내용/ 주장6내용
                        
                        응답 예시:
                        된다: 대중교통은 모든 시민들이 이용할 수 있는 공공재이기 때문에 기본요금을 무료화하여 경제적 부담을 줄여야 한다./ 대중교통 이용 촉진으로 자동차 이용량을 감소시켜 환경 보호에도 도움이 될 것이다./ 저소득층이 대중교통을 더 쉽게 이용할 수 있도록 함으로써 사회적 격차를 줄일 수 있다./
                        안된다: 대중교통 기본요금을 무료화하는 것은 예산 부담이 커지게 되어 다른 사회복지 예산이 부족해질 수 있다./ 무료화로 인해 대중교통 시스템이 지나치게 혼잡해져 서비스 품질이 저하될 우려가 있다./ 무료화로 인해 대중교통을 남용하는 사람들이 늘어나게 되어 정상적인 이용을 하는 시민들에게 불이익을 줄 수 있다./
                        
                        
                        아래와 같은 규칙을 정확히 지켜줘:
                        - 응답은 반드시 한 줄로만 작성해. 줄바꿈 없이 출력해.
                        - 각 주장은 약 100자 이내로 작성해.
                        - 슬래시(/)는 주장 간 구분을 위한 유일한 구분자야. 쉼표, 따옴표 등 다른 기호는 절대 사용하지 마.
                        - 콜론(:)은 반드시 %s: 와 %s: 앞에만 붙여줘.
                        - 위 형식과 정확히 동일하게 출력하지 않으면 잘못된 응답으로 간주돼.
                        - 공격적인 표현은 피하고, 객관적이고 논란이 적은 내용을 사용해.
                        """,
                topic, leftOption, rightOption, leftOption, rightOption, leftOption, rightOption
        );
    }
}