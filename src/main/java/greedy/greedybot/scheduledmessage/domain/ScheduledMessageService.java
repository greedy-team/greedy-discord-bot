package greedy.greedybot.scheduledmessage.domain;

import greedy.greedybot.scheduledmessage.jda.listener.ScheduledMessageScheduler;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

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
