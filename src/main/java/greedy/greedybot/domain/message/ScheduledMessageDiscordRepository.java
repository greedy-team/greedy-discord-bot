package greedy.greedybot.domain.message;

import greedy.greedybot.application.message.dto.ScheduledMessage;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.form.GoogleFormWatch;
import greedy.greedybot.presentation.jda.listener.ScheduledMessageScheduler;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class ScheduledMessageDiscordRepository implements ScheduledMessageRepository {

    private final TextChannel scheduledMessageChannel;

    public ScheduledMessageDiscordRepository(@Lazy TextChannel scheduledMessageChannel) {
        this.scheduledMessageChannel = scheduledMessageChannel;
    }

    @Override
    public void saveScheduledMessage(ScheduledMessage message) {
        scheduledMessageChannel.sendMessage(message.getId() + "|" + message.getContent() + "|" + message.getScheduledTime() + "|" + message.getUserId()).queue();
    }

    @Override
    public void deleteScheduledMessage(String id) {
        scheduledMessageChannel.getHistory().retrievePast(100).complete()
                .stream()
                .filter(message -> message.getContentDisplay().startsWith(id + "|"))
                .findFirst()
                .ifPresentOrElse(
                        message -> message.delete().queue(),
                        () -> { throw new GreedyBotException("❌ 예약된 메시지를 찾을 수 없습니다."); }
                );
    }

    @Override
    public Optional<ScheduledMessage> findByFormId(String formId) {
        return scheduledMessageChannel.getHistory().retrievePast(100).complete()
                .stream()
                .filter(msg -> msg.getContentDisplay().startsWith(formId + "|"))
                .findFirst()
                .map(msg -> {
                    String[] parts = msg.getContentDisplay().split("\\|");
                    return new ScheduledMessage(parts[1], LocalDateTime.parse(parts[2]), parts[3], scheduledMessageChannel.getId());
                });

    }
}
