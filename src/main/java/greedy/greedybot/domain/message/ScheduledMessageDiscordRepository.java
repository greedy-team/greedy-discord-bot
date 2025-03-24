package greedy.greedybot.domain.message;

import greedy.greedybot.common.exception.GreedyBotException;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ScheduledMessageDiscordRepository implements ScheduledMessageRepository {

    private final TextChannel scheduledMessageChannel;

    public ScheduledMessageDiscordRepository(@Lazy TextChannel scheduledMessageChannel) {
        this.scheduledMessageChannel = scheduledMessageChannel;
    }

    @Override
    public void saveScheduledMessage(ScheduledMessage message) {
        scheduledMessageChannel.sendMessage(
                message.getId() + "|" +
                        message.getContent() + "|" +
                        message.getScheduledTime() + "|" +
                        message.getUserId() + "|" +
                        message.getChannelId()
        ).queue();
    }

    @Override
    public void deleteScheduledMessage(String id) {
        scheduledMessageChannel.getHistory().retrievePast(100).complete()
                .stream()
                .filter(message -> message.getContentDisplay().startsWith(id + "|"))
                .findFirst()
                .orElseThrow(() -> new GreedyBotException("❌ 예약된 메시지를 찾을 수 없습니다.")) // ✅ 안전한 예외 처리 방식
                .delete()
                .queue();
    }

    @Override
    public Optional<ScheduledMessage> findById(String formId) {
        return scheduledMessageChannel.getHistory().retrievePast(100).complete()
                .stream()
                .filter(msg -> msg.getContentDisplay().startsWith(formId + "|"))
                .findFirst()
                .map(message -> {
                    String[] parts = message.getContentDisplay().split("\\|");
                    return new ScheduledMessage(
                            parts[0],
                            parts[1],
                            LocalDateTime.parse(parts[2]),
                            parts[3],
                            parts[4]);
                });

    }

    @Override
    public List<ScheduledMessage> findAll() {
        return scheduledMessageChannel.getHistory().retrievePast(100).complete()
                .stream()
                .map(message -> {
                    String[] parts = message.getContentDisplay().split("\\|");
                    if (parts.length < 5)
                        return null;
                    return new ScheduledMessage(
                            parts[0],
                            parts[1],
                            LocalDateTime.parse(parts[2]),
                            parts[3],
                            parts[4]
                    );
                })
                .filter(msg -> msg != null)
                .toList();
    }
}
