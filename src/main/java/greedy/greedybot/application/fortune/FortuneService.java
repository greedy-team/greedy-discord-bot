package greedy.greedybot.application.fortune;

import greedy.greedybot.domain.fortune.FortuneStaticRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Objects;
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
        final long randomSeed = getSeed(userId, today.toEpochDay());
        final int todayFortuneIndex = new Random(randomSeed).nextInt(fortuneSize);
        return fortuneRepository.getFortuneByIndex(todayFortuneIndex);
    }

    public int getSeed(final Long userId, final Long today) {
        final int fortuneSize = fortuneRepository.getSize();
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(String.valueOf(today + userId).getBytes());
            final String encrypt = new String(md.digest());
            return new Random(Objects.hash(encrypt)).nextInt(fortuneSize);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
