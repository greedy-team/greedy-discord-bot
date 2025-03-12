package greedy.greedybot.domain.message;

import greedy.greedybot.application.message.dto.ScheduledMessage;

import java.util.Optional;

public interface ScheduledMessageRepository {

    void save(final ScheduledMessage message);

    void delete(final String formId);

    Optional<ScheduledMessage> findByFormId(final String formId);
}
