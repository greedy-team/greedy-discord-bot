package greedy.greedybot.application.matching;

import greedy.greedybot.application.matching.dto.MatchingResultAnnouncement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        final MatchingResultAnnouncement matchingResultAnnouncement = matchingService.matchStudy(reviewees, reviewers);

        //then

        assertThat(matchingResultAnnouncement.announcement()).contains("리뷰이1    ->   리뷰어1");
        assertThat(matchingResultAnnouncement.announcement()).contains("리뷰이2    ->   리뷰어1");
        assertThat(matchingResultAnnouncement.announcement()).contains("리뷰이3    ->   리뷰어2");
        assertThat(matchingResultAnnouncement.announcement()).contains("리뷰이4    ->   리뷰어2");
    }

    @Test
    @DisplayName("리뷰이보다 리뷰어가 많을때 매칭 테스트")
    void testMatchStudyMoreReviewers() {
        //given
        final List<String> reviewees = List.of("리뷰이1", "리뷰이2") ;
        final List<String> reviewers = List.of("리뷰어1", "리뷰어2", "리뷰어3", "리뷰어4");

        //when
        final MatchingResultAnnouncement matchingResultAnnouncement = matchingService.matchStudy(reviewees, reviewers);

        //then
        assertThat(matchingResultAnnouncement.announcement()).contains("리뷰이1    ->   리뷰어1");
        assertThat(matchingResultAnnouncement.announcement()).contains("리뷰이2    ->   리뷰어2");
    }
}
