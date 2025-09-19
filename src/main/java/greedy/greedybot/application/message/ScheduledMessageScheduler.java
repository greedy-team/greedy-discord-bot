package greedy.greedybot.application.message;

import greedy.greedybot.domain.message.ScheduledMessage;
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

@Component
public class ScheduledMessageScheduler {

    private final static Logger log = LoggerFactory.getLogger(ScheduledMessageScheduler.class);

    private final JDA jda;
    private final ScheduledMessageRepository repository;

    public ScheduledMessageScheduler(@Lazy JDA jda, ScheduledMessageRepository repository) {
        this.jda = jda;
        this.repository = repository;
    }

    @Scheduled(fixedDelay = 60000)
    public void checkScheduledMessages() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledMessage> pendingMessages = repository.findAll();

        for (ScheduledMessage message : pendingMessages) {
            if (message.getScheduledTime().isBefore(now)) {
                sendScheduledMessage(message);
                repository.deleteScheduledMessage(message.getId());
            }
        }
    }

    private void sendScheduledMessage(ScheduledMessage scheduledMessage) {
        TextChannel channel = jda.getTextChannelById(scheduledMessage.getChannelId());

        if (channel == null) {
            log.warn("⚠ 채널을 찾을 수 없습니다. 채널 ID: {}", scheduledMessage.getChannelId());
            return;
        }

        String parsedContent = scheduledMessage.getContent()
            .replaceAll("`<", "<")   // 맨 앞 백틱 제거
            .replaceAll(">`", ">");   // 맨 뒤 백틱 제거

        channel.sendMessage(parsedContent).queue();

        log.info("📢 예약된 메시지가 Discord 채널({})에 전송됨: {}", scheduledMessage.getChannelId(), scheduledMessage.getContent());
    }
}
