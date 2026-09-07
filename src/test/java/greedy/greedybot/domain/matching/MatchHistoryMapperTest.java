package greedy.greedybot.domain.matching;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatchHistoryMapperTest {

    private MatchHistoryMapper matchHistoryMapper;

    @BeforeEach
    void setUp() {
        matchHistoryMapper = new MatchHistoryMapper();
    }

    @Test
    @DisplayName("매칭 기록을 텍스트로 변환한다")
    void toTextEntity() {
        //given
        final Map<String, String> pairs = new LinkedHashMap<>();
        pairs.put("이태규", "정다빈");
        pairs.put("김민욱", "조상준");
        final MatchHistory matchHistory = new MatchHistory("be-4", "장바구니", pairs);

        //when
        final String text = matchHistoryMapper.toTextEntity(matchHistory);

        //then
        assertThat(text).isEqualTo("revieweeGroupId:be-4|mission:장바구니|pairs:이태규>정다빈,김민욱>조상준");
    }

    @Test
    @DisplayName("변환한 텍스트를 다시 매칭 기록으로 복원한다")
    void toTextEntityAndToEntity() {
        //given
        final MatchHistory matchHistory =
            new MatchHistory("fe-4", "영화관", Map.of("천동현", "정창우", "김동건", "김의천"));

        //when
        final MatchHistory restored = matchHistoryMapper.toEntity(matchHistoryMapper.toTextEntity(matchHistory));

        //then
        assertThat(restored).isEqualTo(matchHistory);
    }

    @Test
    @DisplayName("매칭 기록 형식이 아닌 메세지를 구분한다")
    void isMatchHistoryText() {
        //given
        final String historyText = "revieweeGroupId:be-4|mission:장바구니|pairs:이태규>정다빈";
        final String studyGroupText = "id:be-4|role:REVIEWEE|type:BACKEND|generation:4|members:이태규";

        //when & then
        assertThat(matchHistoryMapper.isMatchHistoryText(historyText)).isTrue();
        assertThat(matchHistoryMapper.isMatchHistoryText(studyGroupText)).isFalse();
    }

    @Test
    @DisplayName("지난 매칭에서 같은 리뷰어였는지 확인한다")
    void isSameReviewerAsBefore() {
        //given
        final MatchHistory matchHistory = new MatchHistory("be-4", "장바구니", Map.of("이태규", "정다빈"));

        //when & then
        assertThat(matchHistory.isSameReviewerAsBefore("이태규", "정다빈")).isTrue();
        assertThat(matchHistory.isSameReviewerAsBefore("이태규", "조상준")).isFalse();
        assertThat(matchHistory.isSameReviewerAsBefore("김민욱", "정다빈")).isFalse();
    }
}
