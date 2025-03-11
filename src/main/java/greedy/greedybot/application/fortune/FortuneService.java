package greedy.greedybot.application.fortune;

import greedy.greedybot.domain.fortune.FortuneStaticRepository;
import java.time.LocalDate;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class FortuneService {

    private final FortuneStaticRepository fortuneRepository;

    public FortuneService(final FortuneStaticRepository fortuneRepository) {
        this.fortuneRepository = fortuneRepository;
    }

    public String findTodayFortuneByKey(final Long userId, final LocalDate today) {
        final int fortuneSize = fortuneRepository.getSize();
        final long randomSeed = userId + today.toEpochDay();
        final int todayFortuneIndex = new Random(randomSeed).nextInt(fortuneSize);
        return fortuneRepository.getFortuneByIndex(todayFortuneIndex);
    }
}
