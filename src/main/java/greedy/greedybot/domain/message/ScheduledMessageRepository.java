package greedy.greedybot.domain.message;

import greedy.greedybot.application.message.dto.ScheduledMessage;

import java.util.List;
import java.util.Optional;

public interface ScheduledMessageRepository {

    void saveScheduledMessage(final ScheduledMessage message);

    void deleteScheduledMessage(final String formId);

    Optional<ScheduledMessage> findByFormId(final String formId);

    List<ScheduledMessage> findAll();
}
