package greedy.greedybot.domain.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import greedy.greedybot.common.exception.GreedyBotException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StudyGroupMapperTest {

    private StudyGroupMapper studyGroupMapper;

    @BeforeEach
    void setUp() {
        studyGroupMapper = new StudyGroupMapper();
    }

    @Test
    @DisplayName("스터디 그룹을 텍스트로 변환한다")
    void toTextEntity() {
        //given
        final StudyGroup studyGroup = new StudyGroup(
            "be-4-java", StudyRole.REVIEWER, StudyType.BACKEND, 4, List.of("정다빈", "조상준", "이진"));

        //when
        final String text = studyGroupMapper.toTextEntity(studyGroup);

        //then
        assertThat(text).isEqualTo("id:be-4-java|role:REVIEWER|type:BACKEND|generation:4|members:정다빈,조상준,이진");
    }

    @Test
    @DisplayName("텍스트를 스터디 그룹으로 변환한다")
    void toEntity() {
        //given
        final String text = "id:be-4|role:REVIEWEE|type:BACKEND|generation:4|members:이태규, 김민욱, 이채현";

        //when
        final StudyGroup studyGroup = studyGroupMapper.toEntity(text);

        //then
        assertThat(studyGroup).isEqualTo(
            new StudyGroup("be-4", StudyRole.REVIEWEE, StudyType.BACKEND, 4, List.of("이태규", "김민욱", "이채현")));
    }

    @Test
    @DisplayName("변환한 텍스트를 다시 스터디 그룹으로 복원한다")
    void toTextEntityAndToEntity() {
        //given
        final StudyGroup studyGroup = new StudyGroup(
            "fe-4-1", StudyRole.REVIEWER, StudyType.FRONTEND, 4, List.of("정창우", "김의천"));

        //when
        final StudyGroup restored = studyGroupMapper.toEntity(studyGroupMapper.toTextEntity(studyGroup));

        //then
        assertThat(restored).isEqualTo(studyGroup);
    }

    @Test
    @DisplayName("스터디 그룹 형식이 아닌 메세지를 구분한다")
    void isStudyGroupText() {
        //given
        final String studyGroupText = "id:be-4|role:REVIEWEE|type:BACKEND|generation:4|members:이태규";
        final String chattingText = "오늘 스터디 몇시에 하나요?";

        //when & then
        assertThat(studyGroupMapper.isStudyGroupText(studyGroupText)).isTrue();
        assertThat(studyGroupMapper.isStudyGroupText(chattingText)).isFalse();
    }

    @Test
    @DisplayName("기수가 숫자가 아니면 예외가 발생한다")
    void toEntityWithInvalidGeneration() {
        //given
        final String text = "id:be-4|role:REVIEWEE|type:BACKEND|generation:4기|members:이태규";

        //when & then
        assertThatThrownBy(() -> studyGroupMapper.toEntity(text))
            .isInstanceOf(GreedyBotException.class);
    }

    @Test
    @DisplayName("멤버 이름에 구분자가 포함 되면 예외가 발생한다")
    void createWithForbiddenCharacters() {
        //when & then
        assertThatThrownBy(() -> new StudyGroup(
            "be-4", StudyRole.REVIEWEE, StudyType.BACKEND, 4, List.of("이태규|김민욱")))
            .isInstanceOf(GreedyBotException.class);
    }
}
