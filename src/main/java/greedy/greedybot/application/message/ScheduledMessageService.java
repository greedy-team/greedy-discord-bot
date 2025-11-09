package greedy.greedybot.application.message;

import greedy.greedybot.domain.message.ScheduledMessage;
import greedy.greedybot.domain.message.ScheduledMessageRepository;
import org.springframework.stereotype.Service;

@Service
public class ScheduledMessageService {

    private final ScheduledMessageRepository scheduledMessageRepository;

    public ScheduledMessageService(ScheduledMessageRepository repository) {
        this.scheduledMessageRepository = repository;
    }

    public void scheduleMessage(ScheduledMessage message) {
        scheduledMessageRepository.saveScheduledMessage(message);
    }
}