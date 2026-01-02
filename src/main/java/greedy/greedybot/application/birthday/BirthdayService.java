package greedy.greedybot.application.birthday;

import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.birthday.Birthday;
import greedy.greedybot.domain.birthday.BirthdayRepository;
import greedy.greedybot.domain.birthday.BirthdayStaticRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.security.SecureRandom.getSeed;

@Service
public class BirthdayService {
    private final BirthdayRepository birthdayRepository;
    private final BirthdayStaticRepository birthdayStaticRepository;
    private final MessageTodayData messageTodayData;

    public BirthdayService(BirthdayRepository repository,
                           MessageTodayData messageTodayData,
                           BirthdayStaticRepository birthdayStaticRepository) {
        this.birthdayRepository = repository;
        this.messageTodayData = messageTodayData;
        this.birthdayStaticRepository = birthdayStaticRepository;
    }

    public void register(Birthday birthday) {
        if (birthdayRepository.findByUserId(birthday.getUserId()).isPresent()) {
            throw new GreedyBotException("이미 생일이 등록되어 있습니다.");
        }
        birthdayRepository.save(birthday);
    }

    public Optional<Birthday> findByUserId(String userId) {
        return birthdayRepository.findByUserId(userId);
    }

    public void delete(String userId) {
        if (birthdayRepository.findByUserId(userId).isEmpty()) {
            throw new GreedyBotException("생일이 등록되어 있지 않습니다.");
        }
        birthdayRepository.delete(userId);
    }

    public List<Birthday> findAll() {
        return birthdayRepository.findAll();
    }

    public List<Birthday> findTodayBirthdays() {
        MonthDay now = MonthDay.now();
        return birthdayRepository.findByMonthDay(now.getMonthValue(), now.getDayOfMonth());
    }

    public String pickMessage(LocalDate today) {
        //final long randomSeed = today.toEpochDay();
        final int[] probabilityBox = messageTodayData.probabilityBox();
       // final int todayMessageIndex = new Random(randomSeed).nextInt(probabilityBox.length);
        final int todayMessageIndex = new Random().nextInt(probabilityBox.length);
        return birthdayStaticRepository.getMessageByIndex(probabilityBox[todayMessageIndex]);
    }

    public void clearAll() {
        birthdayRepository.clearAll();
    }
}