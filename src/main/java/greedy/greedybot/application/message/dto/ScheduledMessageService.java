package greedy.greedybot.application.message.dto;

import greedy.greedybot.presentation.jda.listener.ScheduledMessageScheduler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ScheduledMessageService {

    private final ScheduledMessageRepository scheduledMessageRepository;
    private final ScheduledMessageScheduler scheduledMessageScheduler;

    public ScheduledMessageService(ScheduledMessageRepository repository, @Lazy ScheduledMessageScheduler scheduler) {
        this.scheduledMessageRepository = repository;
        this.scheduledMessageScheduler = scheduler;
    }

    public void scheduleMessage(ScheduledMessage message){
        scheduledMessageRepository.save(message);
    }
}
