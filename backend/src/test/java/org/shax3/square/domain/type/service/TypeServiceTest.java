package org.shax3.square.domain.type.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shax3.square.domain.type.dto.request.EndTypeTestRequest;
import org.shax3.square.domain.type.dto.request.TypeTestAnswer;
import org.shax3.square.domain.type.dto.response.TypeInfoResponse;
import org.shax3.square.domain.type.dto.response.TypeTestQuestionResponse;
import org.shax3.square.domain.type.model.Question;
import org.shax3.square.domain.type.model.TypeResult;
import org.shax3.square.domain.type.repository.QuestionRepository;
import org.shax3.square.domain.type.repository.TypeRepository;
import org.shax3.square.domain.user.model.Type;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.domain.user.service.UserService;
import org.shax3.square.exception.CustomException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.shax3.square.exception.ExceptionCode.TYPE_RESULT_NOT_FOUND;
import static org.shax3.square.exception.ExceptionCode.USER_TYPE_NOT_FOUND;

@ExtendWith(MockitoExtension.class)
public class TypeServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private TypeRepository typeRepository;

    @Mock
    private UserService userService;

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

    @Test
    @DisplayName("성향테스트 종료 - 결과 계산 및 저장 테스트")
    public void endTypeTest_ShouldReturnCorrectResponseAndUpdateUserAndRepository() {
        // given
        // 각 그룹(8문항)마다 홀수 질문(answer=5)와 짝수 질문(answer=3)을 주어
        // 홀수 질문: 5 -> (5-3)=2, 짝수 질문: 3 -> (3-3)=0 -> 0<=0 이므로 -1, 그리고 음수일 경우 부호 변경하여 +1
        // 각 그룹의 총합: (2*4)+(1*4)=12, 최종 점수 = (int)(12/8.0+0.5) = 2
        List<TypeTestAnswer> answers = new ArrayList<>();
        for (int i = 1; i <= 32; i++) {
            int answerValue = (i % 2 == 1) ? 5 : 3;
            answers.add(new TypeTestAnswer(i, answerValue));
        }
        EndTypeTestRequest request = new EndTypeTestRequest(answers);

        User user = mock(User.class);
        when(user.getNickname()).thenReturn("TestUser");

        // when
        TypeInfoResponse response = typeService.endTypeTest(user, request);

        // then
        int[] expectedScore = {2, 2, 2, 2};
        // 타입 결정: score>=0 이므로 Type1="I", Type2="C", Type3="S", Type4="R"
        String expectedTypeString = "ICSR";

        assertThat(response).isNotNull();
        assertThat(response.nickname()).isEqualTo("TestUser");
        assertThat(response.userType()).isEqualTo(expectedTypeString);
        assertThat(response.score1()).isEqualTo(expectedScore[0]);
        assertThat(response.score2()).isEqualTo(expectedScore[1]);
        assertThat(response.score3()).isEqualTo(expectedScore[2]);
        assertThat(response.score4()).isEqualTo(expectedScore[3]);

        verify(typeRepository).save(any(TypeResult.class));

        verify(user).updateType(Type.valueOf(expectedTypeString));
    }

    @Test
    @DisplayName("내 타입 정보 조회(getMyTypeInfo) - 정상 케이스")
    public void getMyTypeInfo_ShouldReturnCorrectResponse() {
        // given
        User user = mock(User.class);
        when(user.getNickname()).thenReturn("TestUser");
        when(user.getType()).thenReturn(Type.valueOf("ICSR"));
        int[] scores = new int[]{2, 2, 2, 2};
        TypeResult typeResult = TypeResult.builder()
                .score(scores)
                .user(user)
                .build();
        when(typeRepository.findByUser(user)).thenReturn(Optional.of(typeResult));

        // when
        TypeInfoResponse response = typeService.getMyTypeInfo(user);

        // then
        assertThat(response).isNotNull();
        assertThat(response.nickname()).isEqualTo("TestUser");
        assertThat(response.userType()).isEqualTo("ICSR");
        assertThat(response.score1()).isEqualTo(scores[0]);
        assertThat(response.score2()).isEqualTo(scores[1]);
        assertThat(response.score3()).isEqualTo(scores[2]);
        assertThat(response.score4()).isEqualTo(scores[3]);
    }

    @Test
    @DisplayName("내 타입 정보 조회(getMyTypeInfo) - 결과 미존재 시 예외 발생")
    public void getMyTypeInfo_NotFound_ShouldThrowException() {
        // given
        User user = mock(User.class);
        when(typeRepository.findByUser(user)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> typeService.getMyTypeInfo(user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(TYPE_RESULT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("닉네임으로 타입 정보 조회(getTypeInfo) - 정상 케이스")
    public void getTypeInfo_ShouldReturnCorrectResponse() {
        // given
        String nickname = "TestUser";
        User user = mock(User.class);
        when(userService.findByNickname(nickname)).thenReturn(user);
        when(user.getNickname()).thenReturn(nickname);
        when(user.getType()).thenReturn(Type.valueOf("ICSR"));

        int[] scores = new int[]{2, 2, 2, 2};
        TypeResult typeResult = TypeResult.builder()
                .score(scores)
                .user(user)
                .build();
        when(typeRepository.findByUser(user)).thenReturn(Optional.of(typeResult));

        // when
        TypeInfoResponse response = typeService.getTypeInfo(nickname);

        // then
        assertThat(response).isNotNull();
        assertThat(response.nickname()).isEqualTo(nickname);
        assertThat(response.userType()).isEqualTo("ICSR");
        assertThat(response.score1()).isEqualTo(scores[0]);
        assertThat(response.score2()).isEqualTo(scores[1]);
        assertThat(response.score3()).isEqualTo(scores[2]);
        assertThat(response.score4()).isEqualTo(scores[3]);
        verify(userService).findByNickname(nickname);
    }

    @Test
    @DisplayName("닉네임으로 타입 정보 조회 - 유저 타입이 null일 경우 예외 발생")
    void getTypeInfo_ShouldThrowException_WhenUserTypeIsNull() {
        // given
        String nickname = "TestUser";
        User user = mock(User.class);
        when(userService.findByNickname(nickname)).thenReturn(user);
        when(user.getType()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> typeService.getTypeInfo(nickname))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(USER_TYPE_NOT_FOUND.getMessage());

        verify(userService).findByNickname(nickname);
        verify(user, times(1)).getType();
    }

}
