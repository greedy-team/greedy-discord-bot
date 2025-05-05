package greedy.greedybot.application.matching;

import greedy.greedybot.application.matching.dto.MatchingResult;
import greedy.greedybot.common.exception.GreedyBotException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MatchingServiceTest {
    private MatchingService matchingService;

    @BeforeEach
    void setUp() {
        matchingService = new MatchingService(new MockShuffleStrategy());
    }

    @Test
    @DisplayName("리뷰어보다 리뷰이가 많을때 매칭 테스트")
    void testMatchStudyMoreReviewees() {
        //given
        final List<String> reviewees = List.of("리뷰이1", "리뷰이2", "리뷰이3", "리뷰이4") ;
        final List<String> reviewers = List.of("리뷰어1", "리뷰어2");

        //when
        final MatchingResult matchingResult = matchingService.matchStudy(reviewees, reviewers);

        //then
        final String announcement = matchingResult.toDiscordAnnouncement();
        assertThat(announcement).contains("리뷰이1    ->   리뷰어1");
        assertThat(announcement).contains("리뷰이2    ->   리뷰어2");
        assertThat(announcement).contains("리뷰이3    ->   리뷰어1");
        assertThat(announcement).contains("리뷰이4    ->   리뷰어2");
    }

    @Test
    @DisplayName("리뷰이보다 리뷰어가 많을때 매칭 테스트")
    void testMatchStudyMoreReviewers() {
        //given
        final List<String> reviewees = List.of("리뷰이1", "리뷰이2") ;
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
        final List<String> reviewees = List.of("태연", "해윤") ;
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

}
