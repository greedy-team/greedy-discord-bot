package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.application.message.ScheduledMessageService;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.message.ScheduledMessage;
import greedy.greedybot.presentation.jda.role.ScheduledMessageChannel;
import greedy.greedybot.presentation.jda.role.ScheduledMessageChannels;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ScheduledMessageSubmitListener extends ListenerAdapter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm",
        Locale.ENGLISH);

    private final ScheduledMessageService scheduledMessageService;
    private final ScheduledMessageChannels scheduledMessageChannels;
    private static final Logger log = LoggerFactory.getLogger(ScheduledMessageSubmitListener.class);

    public ScheduledMessageSubmitListener(ScheduledMessageService scheduledMessageService,
        ScheduledMessageChannels scheduledMessageChannels) {
        this.scheduledMessageService = scheduledMessageService;
        this.scheduledMessageChannels = scheduledMessageChannels;
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        try {
            String[] parts = event.getModalId().split(":", 3);

            String channelString = parts[1];
            String members = (parts.length > 2) ? parts[2] : null;

            final ScheduledMessageChannel channelEnum = ScheduledMessageChannel.valueOf(channelString);
            final long resolvedChannelId = scheduledMessageChannels.getChannelId(channelEnum);

            final String message = event.getValue("message").getAsString();
            final String timeString = event.getValue("time").getAsString();

            // 시간 관련 검증
            LocalDateTime time = parseScheduledTime(timeString); //형식 검증
            isValidScheduledTime(time); //과거 시간 입력 검증

            String finalMessage = message;
            if (members != null && !members.isEmpty()) {
                finalMessage = "`" + members + "`" + "\n" + message;
            }

            final ScheduledMessage scheduledMessage = new ScheduledMessage(
                finalMessage,
                time,
                event.getUser().getId(),
                String.valueOf(resolvedChannelId)
            );

            scheduledMessageService.scheduleMessage(scheduledMessage);

            log.info("✅ 메시지가 " + resolvedChannelId + " 채널에 예약되었습니다.");
            event.reply("✅ 메시지가 " + timeString + "에 예약되었습니다.").queue();
        } catch (GreedyBotException e) {
            log.error(e.getMessage());
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    public LocalDateTime parseScheduledTime(String timeString) {
        try {
            return LocalDateTime.parse(timeString, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.error("❌ 잘못된 시간 형식: {}", timeString);
            throw new GreedyBotException("❌ 잘못된 시간 형식입니다! yyyy-MM-dd HH:mm 형식으로 입력해주세요.");
        }
    }

    public void isValidScheduledTime(LocalDateTime time) {
        final LocalDateTime now = LocalDateTime.now();

        if (time.isBefore(now)) {
            log.error("❌ 잘못된 예약 시간: {} (과거 시간 입력됨)", time);
            throw new GreedyBotException("❌ 예약할 시간은 현재 시간 이후여야 합니다!");
        }
    }
}
