package org.shax3.square.domain.type.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.type.dto.request.EndTypeTestRequest;
import org.shax3.square.domain.type.dto.request.TypeTestAnswer;
import org.shax3.square.domain.type.dto.response.TypeInfoResponse;
import org.shax3.square.domain.type.dto.response.TypeTestQuestion;
import org.shax3.square.domain.type.dto.response.TypeTestQuestionResponse;
import org.shax3.square.domain.type.model.Question;
import org.shax3.square.domain.type.model.TypeResult;
import org.shax3.square.domain.type.model.Type1;
import org.shax3.square.domain.type.model.Type2;
import org.shax3.square.domain.type.model.Type3;
import org.shax3.square.domain.type.model.Type4;
import org.shax3.square.domain.type.repository.QuestionRepository;
import org.shax3.square.domain.type.repository.TypeRepository;
import org.shax3.square.domain.user.model.Type;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.shax3.square.exception.ExceptionCode.*;

@Service
@RequiredArgsConstructor
public class TypeService {

    private static List<Question> staticQuestionList;

    private final QuestionRepository questionRepository;
    private final TypeRepository typeRepository;

    @PostConstruct
    public void init() {
        staticQuestionList = questionRepository.findAll();
    }

    public TypeTestQuestionResponse getShuffledQuestionList() {

        List<TypeTestQuestion> questionList = new ArrayList<>(staticQuestionList.stream()
                .map(q -> new TypeTestQuestion(q.getId(), q.getContent()))
                .toList());

        Collections.shuffle(questionList);

        return TypeTestQuestionResponse.from(questionList);
    }

    @Transactional
    public TypeInfoResponse endTypeTest(User user, EndTypeTestRequest endTypeTestRequest) {
        int[] score = new int[4];
        int[] answered = new int[33];

        for (TypeTestAnswer answer : endTypeTestRequest.answers()) {
            calculateScore(score, answered, answer);
        }

        for (int i = 0; i < 4; i++) {
            score[i] = (int)(score[i]/8.0 + 0.5);
            score[i] = checkZero(score[i]);
        }

        Type1 type1 = Type1.valueOf(score[0] < 0 ? "P" : "I");
        Type2 type2 = Type2.valueOf(score[1] < 0 ? "N" : "C");
        Type3 type3 = Type3.valueOf(score[2] < 0 ? "T" : "S");
        Type4 type4 = Type4.valueOf(score[3] < 0 ? "B" : "R");

        TypeResult typeResult = TypeResult.builder()
                .user(user)
                .type1(type1)
                .type2(type2)
                .type3(type3)
                .type4(type4)
                .score(score)
                .build();

        typeRepository.deleteByUser(user);
        typeRepository.save(typeResult);

        String typeInString = type1.name() + type2.name() + type3.name() + type4.name();
        Type type = Type.valueOf(typeInString);

        user.updateType(type);

        return TypeInfoResponse.of(user.getNickname(), typeInString, score);
    }

    private int checkZero(int score) {
        if (score == 0) {
            return Math.random() < 0.5 ? -1 : 1;
        }
        return score;
    }

    private void calculateScore(int[] score, int[] answered, TypeTestAnswer answer) {
        int questionId = answer.questionId();

        if (answered[questionId] > 0) {
            throw new CustomException(TYPE_QUESTION_DUPLICATION);
        }
        answered[questionId]++;

        int testAnswer = answer.answer() - 3;

        if (testAnswer <= 0) {
            testAnswer -= 1;
        }

        if (questionId % 2 == 0) {
            testAnswer *= -1;
        }

        int index = (questionId - 1) / 8;
        score[index] += testAnswer;
    }
}
