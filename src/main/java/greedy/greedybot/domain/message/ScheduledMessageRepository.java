package greedy.greedybot.domain.message;

import java.util.List;
import java.util.Optional;

public interface ScheduledMessageRepository {

    void saveScheduledMessage(final ScheduledMessage message);

    void deleteScheduledMessage(final String formId);

    Optional<ScheduledMessage> findById(final String formId);

    List<ScheduledMessage> findAll();
}
