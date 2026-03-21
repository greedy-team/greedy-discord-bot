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

    public BirthdayService(BirthdayRepository repository,
                           BirthdayStaticRepository birthdayStaticRepository) {
        this.birthdayRepository = repository;
        this.birthdayStaticRepository = birthdayStaticRepository;
    }

    public void register(Birthday birthday) {
//        if (birthdayRepository.findByUserId(birthday.getUserId()).isPresent()) {
//            delete(birthday.getUserId());
//        }
//        birthdayRepository.findByUserId(birthday.getUserId())
//                .ifPresent(existing -> {
//                    birthdayRepository.delete(birthday.getUserId());
//                });

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
        return birthdayStaticRepository.getMessageBySeason(today.getMonthValue());
    }

    public void clearAll() {
        birthdayRepository.clearAll();
    }
}
