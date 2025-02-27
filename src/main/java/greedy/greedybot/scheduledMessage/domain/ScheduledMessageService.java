package greedy.greedybot.scheduledMessage.domain;

import greedy.greedybot.scheduledMessage.jda.listener.ScheduledMessageScheduler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;

@Service
public class ScheduledMessageService {

    private final ScheduledMessageRepository repository;
    private final ScheduledMessageScheduler scheduler;

    public ScheduledMessageService(ScheduledMessageRepository repository, @Lazy ScheduledMessageScheduler scheduler) {
        this.repository = repository;
        this.scheduler = scheduler;
    }

    public void scheduleMessage(ScheduledMessage message){
        repository.save(message);
        scheduler.schedule(message, () -> repository.delete(message.getId()));
    }
}
