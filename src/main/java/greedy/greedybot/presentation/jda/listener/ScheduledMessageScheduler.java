package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.application.message.dto.ScheduledMessage;
import greedy.greedybot.domain.message.ScheduledMessageRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class ScheduledMessageScheduler {

    private final Logger log = LoggerFactory.getLogger(StatusCommandListener.class);
    private final JDA jda;
    private final ScheduledMessageRepository repository;

    public ScheduledMessageScheduler(@Lazy JDA jda, ScheduledMessageRepository repository) {
        this.jda = jda;
        this.repository = repository;
    }

    @Scheduled(fixedDelay = 30000)
    public void checkScheduledMessages() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledMessage> pendingMEssages = repository.findAll();

        for (ScheduledMessage message : pendingMEssages) {
            if (message.getScheduledTime().isBefore(now)) {
                sendScheduledMessage(message);
                repository.deleteScheduledMessage(message.getId());
            }
        }
    }

    private void sendScheduledMessage(ScheduledMessage scheduledMessage) {
        TextChannel channel = jda.getTextChannelById(scheduledMessage.getChannelId());

        channel.sendMessage(scheduledMessage.getContent()).queue();
        log.info("📢 예약된 메시지가 Discord 채널({})에 전송됨: {}", scheduledMessage.getContent(), scheduledMessage.getContent());

    }
}
