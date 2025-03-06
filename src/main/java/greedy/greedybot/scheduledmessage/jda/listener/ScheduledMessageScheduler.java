package greedy.greedybot.scheduledmessage.jda.listener;

import greedy.greedybot.presentation.jda.listener.StatusCommandListener;
import greedy.greedybot.scheduledmessage.domain.ScheduledMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;

@Component
public class ScheduledMessageScheduler {

    private final Logger log = LoggerFactory.getLogger(StatusCommandListener.class);
    private final JDA jda;

    public ScheduledMessageScheduler(@Lazy JDA jda) {
        this.jda = jda;
    }

    public void schedule(ScheduledMessage scheduledMessage, Runnable runnable) {
        Long delay = scheduledMessage.getScheduledTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendScheduledMessage(scheduledMessage);
                runnable.run();
            }
        }, delay);
    }

    private void sendScheduledMessage(ScheduledMessage scheduledMessage) {
        TextChannel channel = jda.getTextChannelById(scheduledMessage.getChannelId());

        channel.sendMessage(scheduledMessage.getContent()).queue();
        log.info("📢 예약된 메시지가 Discord 채널({})에 전송됨: {}", scheduledMessage.getContent(), scheduledMessage.getContent());

    }
}
