import { Debate } from "./Debate.types";

export const debateData: Debate[] = [
    {
        debateId: 0,
        category: "테스트",
        topic: "개 vs 고양이",
        leftOption: "개",
        rightOption: "고양이",
        isScraped: false,
        isLeft: true,
        leftCount: 80,
        rightCount: 20,
        leftPercent: 80,
        rightPercent: 20,
        totalVoteCount: 100,
    },
    {
        debateId: 1,
        category: "연애",
        topic: "연애에서 성격 vs 외모, 더 중요한 것은?",
        leftOption: "성격",
        rightOption: "외모",
        isScraped: false,
        isLeft: null,
        leftCount: 80,
        rightCount: 70,
        leftPercent: 53,
        rightPercent: 47,
        totalVoteCount: 150,
    },
    {
        debateId: 2,
        category: "직장",
        topic: "연봉 vs 워라밸, 직장 선택 기준은?",
        leftOption: "연봉",
        rightOption: "워라밸",
        isScraped: true,
        isLeft: true,
        leftCount: 85,
        rightCount: 15,
        leftPercent: 85,
        rightPercent: 15,
        totalVoteCount: 100,
    },
    {
        debateId: 3,
        category: "대학생",
        topic: "조별 과제, 리더가 해야 할까? 역할 분배가 중요할까?",
        leftOption: "리더 역할 중요",
        rightOption: "역할 분배 중요",
        isScraped: false,
        isLeft: null,
        leftCount: 50,
        rightCount: 70,
        leftPercent: 42,
        rightPercent: 58,
        totalVoteCount: 120,
    },
    {
        debateId: 4,
        category: "다이어트",
        topic: "체중 감량, 운동이 더 중요할까? 식단이 더 중요할까?",
        leftOption: "운동",
        rightOption: "식단",
        isScraped: true,
        isLeft: false,
        leftCount: 10,
        rightCount: 90,
        leftPercent: 10,
        rightPercent: 90,
        totalVoteCount: 100,
    },
    {
        debateId: 5,
        category: "재테크",
        topic: "투자 vs 저축, 돈을 모으는 최선의 방법은?",
        leftOption: "투자",
        rightOption: "저축",
        isScraped: false,
        isLeft: null,
        leftCount: 80,
        rightCount: 60,
        leftPercent: 57,
        rightPercent: 43,
        totalVoteCount: 140,
    },
    {
        debateId: 6,
        category: "SNS",
        topic: "SNS가 삶의 질을 높일까? 낮출까?",
        leftOption: "높인다",
        rightOption: "낮춘다",
        isScraped: true,
        isLeft: true,
        leftCount: 110,
        rightCount: 90,
        leftPercent: 55,
        rightPercent: 45,
        totalVoteCount: 200,
    },
    {
        debateId: 7,
        category: "자취",
        topic: "자취할 때 필수 가전제품, 전자레인지 vs 세탁기?",
        leftOption: "전자레인지",
        rightOption: "세탁기",
        isScraped: false,
        isLeft: null,
        leftCount: 95,
        rightCount: 65,
        leftPercent: 59,
        rightPercent: 41,
        totalVoteCount: 160,
    },
    {
        debateId: 8,
        category: "출퇴근",
        topic: "출퇴근, 대중교통 vs 자차 어떤 게 더 나을까?",
        leftOption: "대중교통",
        rightOption: "자차",
        isScraped: true,
        isLeft: false,
        leftCount: 85,
        rightCount: 85,
        leftPercent: 50,
        rightPercent: 50,
        totalVoteCount: 170,
    },
    {
        debateId: 9,
        category: "음악",
        topic: "공부할 때 음악을 듣는 게 도움이 될까? 방해가 될까?",
        leftOption: "도움 된다",
        rightOption: "방해된다",
        isScraped: false,
        isLeft: null,
        leftCount: 70,
        rightCount: 60,
        leftPercent: 54,
        rightPercent: 46,
        totalVoteCount: 130,
    },
    {
        debateId: 10,
        category: "수면",
        topic: "수면 시간 vs 수면의 질, 더 중요한 것은?",
        leftOption: "수면 시간",
        rightOption: "수면의 질",
        isScraped: true,
        isLeft: null,
        leftCount: 85,
        rightCount: 75,
        leftPercent: 53,
        rightPercent: 47,
        totalVoteCount: 160,
    }
];
