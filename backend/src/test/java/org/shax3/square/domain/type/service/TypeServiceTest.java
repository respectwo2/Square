package org.shax3.square.domain.type.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shax3.square.domain.type.dto.response.TypeTestQuestionResponse;
import org.shax3.square.domain.type.model.Question;
import org.shax3.square.domain.type.repository.QuestionRepository;

@ExtendWith(MockitoExtension.class)
public class TypeServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private TypeService typeService;

    private List<Question> questions;

    private Question createQuestion(Long id, String content, String category, boolean direction) {
        try {
            Constructor<Question> constructor = Question.class.getDeclaredConstructor(
                    Long.class, String.class, String.class, boolean.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(id, content, category, direction);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void setUp() {
        questions = Arrays.asList(
                createQuestion(1L, "Question 1?", "가치관", true),
                createQuestion(2L, "Question 2?", "가치관", true),
                createQuestion(3L, "Question 3?", "가치관", true)
        );
        when(questionRepository.findAll()).thenReturn(questions);
        typeService.init();
    }

    @Test
    @DisplayName("성향테스트 질문 목록 순서 랜덤으로 조회")
    public void getShuffledQuestionList_ShouldReturnCorrectResponse() {
        // when
        TypeTestQuestionResponse response = typeService.getShuffledQuestionList();

        // then
        assertThat(response).isNotNull();
        assertThat(response.questions()).hasSize(questions.size());
        for (Question question : questions) {
            assertThat(response.questions())
                    .anyMatch(q -> q.questionId().equals(question.getId())
                            && q.content().equals(question.getContent()));
        }
    }
}
