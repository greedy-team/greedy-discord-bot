package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.domain.message.ScheduledMessage;
import greedy.greedybot.application.message.ScheduledMessageService;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import java.util.Set;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Component
public class ScheduledCommandListener implements SlashCommandListener {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH);
    private static final Logger log = LoggerFactory.getLogger(ScheduledCommandListener.class);

    private final ScheduledMessageService scheduledMessageService;

    public ScheduledCommandListener(ScheduledMessageService scheduledMessageService) {
        this.scheduledMessageService = scheduledMessageService;
    }

    @Override
    public String getCommandName() {
        return "add-scheduled-message";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("add-scheduled-message", "Schedule a message")
                .addOption(OptionType.STRING, "message", "The message to send", true)
                .addOption(OptionType.STRING, "time", "The time to send (yyyy-MM-dd HH:mm)", true);
    }

    @Override
    public void onAction(@NotNull SlashCommandInteractionEvent event) {
        try {
            final String message = event.getOption("message").getAsString();
            final String timeString = event.getOption("time").getAsString();
            final String channelId = event.getChannelId(); // 명령어 입력한 채널의 ID 저장

            // 1. 시간 형식 검증
            LocalDateTime time = parseScheduledTime(timeString);
            // 2. 과거 시간 입력 여부 확인
            isValidScheduledTime(time);

            event.deferReply().queue();

            final ScheduledMessage scheduledMessage = new ScheduledMessage(message, time, event.getUser().getId(), channelId);
            scheduledMessageService.scheduleMessage(scheduledMessage);

            log.info("✅ 메시지가 "+ channelId+" 채널에 예약되었습니다.");
            event.getHook().sendMessage("✅ 메시지가 " + timeString + "에 예약되었습니다.").queue();
        } catch (GreedyBotException e) {
            log.error(e.getMessage());
            event.getHook().sendMessage(e.getMessage()).setEphemeral(true).queue();
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

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.LEAD);
    }
}
