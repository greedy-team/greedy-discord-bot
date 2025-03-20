package greedy.greedybot.application.fortune;

import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.fortune.FortuneStaticRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class FortuneService {

    private static final MessageDigest md = createDigest();

    private final FortuneStaticRepository fortuneRepository;
    private final FortuneTodayData fortuneTodayData;

    public FortuneService(final FortuneStaticRepository fortuneRepository, final FortuneTodayData fortuneTodayData) {
        this.fortuneRepository = fortuneRepository;
        this.fortuneTodayData = fortuneTodayData;
    }

    public String findTodayFortuneByKey(final Long userId, final LocalDate today) {
        final long randomSeed = getSeed(userId, today.toEpochDay());
        final int[] probabilityBox = this.fortuneTodayData.probabilityBox();
        final int todayFortuneIndex = new Random(randomSeed).nextInt(probabilityBox.length);
        return fortuneRepository.getFortuneByIndex(probabilityBox[todayFortuneIndex]);
    }

    public int getSeed(final Long userId, final Long today) {
        final int dailyOffset = new Random(today).nextInt(100) + 1;
        md.update(String.valueOf(today + userId).getBytes());
        final String encrypt = new String(md.digest());
        return Objects.hash(encrypt) * dailyOffset;
    }

    private static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
