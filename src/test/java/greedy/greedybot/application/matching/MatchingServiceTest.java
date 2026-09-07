package greedy.greedybot.application.matching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import greedy.greedybot.application.matching.dto.MatchingResult;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.matching.MatchHistory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class MatchingServiceTest {
    private MatchingService matchingService;

    @BeforeEach
    void setUp() {
        matchingService = new MatchingService(new MockShuffleStrategy());
    }

    @Test
    @DisplayName("리뷰어보다 리뷰이가 많으면 매칭하지 않는다")
    void testMatchStudyMoreReviewees() {
        //given
        final List<String> reviewees = List.of("리뷰이1", "리뷰이2", "리뷰이3", "리뷰이4");
        final List<String> reviewers = List.of("리뷰어1", "리뷰어2");

        //when & then
        assertThatThrownBy(() -> matchingService.matchStudy(reviewees, reviewers))
                .isInstanceOf(GreedyBotException.class)
                .hasMessageContaining("리뷰어가 리뷰이보다 적습니다");
    }

    @Test
    @DisplayName("리뷰이보다 리뷰어가 많을때 매칭 테스트")
    void testMatchStudyMoreReviewers() {
        //given
        final List<String> reviewees = List.of("리뷰이1", "리뷰이2");
        final List<String> reviewers = List.of("리뷰어1", "리뷰어2", "리뷰어3", "리뷰어4");

        //when
        final MatchingResult matchingResult = matchingService.matchStudy(reviewees, reviewers);

        //then
        final String announcement = matchingResult.toDiscordAnnouncement();
        assertThat(announcement).contains("리뷰이1    ->   리뷰어1");
        assertThat(announcement).contains("리뷰이2    ->   리뷰어2");
    }

    @RepeatedTest(20)
    @DisplayName("리뷰어와 리뷰이가 같을때 매칭 테스트")
    void testMatchStudyDoesNotMatchSamePerson() {
        //given
        final List<String> reviewees = List.of("태연", "해윤");
        final List<String> reviewers = List.of("태연", "해윤");

        //when
        final MatchingResult matchingResult = matchingService.matchStudy(reviewees, reviewers);

        //then
        final String announcement = matchingResult.toDiscordAnnouncement();
        assertThat(announcement).doesNotContain("태연    ->   태연");
        assertThat(announcement).doesNotContain("해윤    ->   해윤");
    }

    @Test
    @DisplayName("경우의 수가 존재하지 않을때 매칭 테스트")
    void testMatchStudyDoesNotMatchNoCases() {
        //given
        final List<String> reviewees = List.of("태연", "승준");
        final List<String> reviewers = List.of("태연", "태연"); // 동일한 이름의 리뷰이-리뷰어 매칭이 무조건 발생

        //when & then
        assertThatThrownBy(() -> matchingService.matchStudy(reviewees, reviewers))
                .isInstanceOf(GreedyBotException.class)
                .hasMessageContaining("매칭 경우의 수가 존재 하지 않습니다");
    }


    @RepeatedTest(10)
    @DisplayName("지난 매칭과 같은 리뷰어를 피해서 다시 매칭한다")
    void testMatchStudyAvoidsPreviousReviewer() {
        //given
        final MatchingService randomMatchingService = new MatchingService(new RandomShuffleStrategy());
        final List<String> reviewees = List.of("리뷰이1", "리뷰이2", "리뷰이3", "리뷰이4");
        final List<String> reviewers = List.of("리뷰어1", "리뷰어2", "리뷰어3", "리뷰어4");
        final MatchHistory previousMatch = new MatchHistory("be-4", "지난미션", Map.of(
            "리뷰이1", "리뷰어1",
            "리뷰이2", "리뷰어2",
            "리뷰이3", "리뷰어3",
            "리뷰이4", "리뷰어4"
        ));

        //when
        final MatchingResult matchingResult = randomMatchingService.matchStudy(reviewees, reviewers, previousMatch);

        //then
        assertThat(matchingResult.repeatedCount()).isZero();
        assertThat(matchingResult.toReviewerByReviewee()).allSatisfy(
            (reviewee, reviewer) -> assertThat(previousMatch.isSameReviewerAsBefore(reviewee, reviewer)).isFalse());
    }

    @Test
    @DisplayName("겹치지 않는 조합이 없으면 겹치는 매칭을 표시해서 반환한다")
    void testMatchStudyWhenRepeatIsUnavoidable() {
        //given
        final MatchingService randomMatchingService = new MatchingService(new RandomShuffleStrategy());
        final List<String> reviewees = List.of("리뷰이1");
        final List<String> reviewers = List.of("리뷰어1");
        final MatchHistory previousMatch =
            new MatchHistory("be-4", "지난미션", Map.of("리뷰이1", "리뷰어1"));

        //when
        final MatchingResult matchingResult = randomMatchingService.matchStudy(reviewees, reviewers, previousMatch);

        //then
        assertThat(matchingResult.repeatedCount()).isEqualTo(1);
        assertThat(matchingResult.toDiscordAnnouncement()).contains("⚠️ 지난 미션과 동일");
    }

    @Test
    @DisplayName("지난 매칭 기록이 없으면 그대로 매칭한다")
    void testMatchStudyWithoutPreviousMatch() {
        //given
        final List<String> reviewees = List.of("리뷰이1", "리뷰이2");
        final List<String> reviewers = List.of("리뷰어1", "리뷰어2");

        //when
        final MatchingResult matchingResult =
            matchingService.matchStudy(reviewees, reviewers, MatchHistory.empty("be-4"));

        //then
        assertThat(matchingResult.repeatedCount()).isZero();
        assertThat(matchingResult.toDiscordAnnouncement()).doesNotContain("⚠️");
    }
}
