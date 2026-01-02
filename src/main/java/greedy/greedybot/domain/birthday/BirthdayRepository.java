package greedy.greedybot.domain.birthday;

import java.util.List;
import java.util.Optional;

public interface BirthdayRepository {
    void save(final Birthday birthday);

    void delete(final String userId);

    Optional<Birthday> findByUserId(final String userId);

    List<Birthday> findAll();

    List<Birthday> findByMonthDay(final int month, final int day);

    public void clearAll();

}