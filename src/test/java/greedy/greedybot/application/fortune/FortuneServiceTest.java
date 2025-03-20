package greedy.greedybot.application.fortune;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FortuneServiceTest {

    @Autowired
    private FortuneService fortuneService;

    @Test
    void findTodayFortuneByKey() {
        // given
        Long userId = 96L;
        LocalDate today = LocalDate.of(2025, 3, 20);

        // when
        String resultA = fortuneService.findTodayFortuneByKey(userId, today);
        String resultB = fortuneService.findTodayFortuneByKey(userId, today);
        String resultC = fortuneService.findTodayFortuneByKey(userId, today);

        // then
        assertThat(resultA).isEqualTo(resultB);
        assertThat(resultB).isEqualTo(resultC);
    }
}
