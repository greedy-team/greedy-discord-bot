package greedy.greedybot.application.fortune;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import greedy.greedybot.domain.fortune.FortuneStaticRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

class FortuneServiceTest {

    private final FortuneService fortuneService = new FortuneService(
            new FortuneStaticRepository()
    );

    @Test
    void findTodayFortuneByKey() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.of(2021, 10, 1);

        // when
        String resultA = fortuneService.findTodayFortuneByKey(userId, today);
        String resultB = fortuneService.findTodayFortuneByKey(userId, today);
        String resultC = fortuneService.findTodayFortuneByKey(userId, today);

        // then
        assertThat(resultA).isEqualTo(resultB);
        assertThat(resultB).isEqualTo(resultC);
    }
}
