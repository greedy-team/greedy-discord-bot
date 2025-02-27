package greedy.greedybot.scheduledMessage.jda.listener;

import greedy.greedybot.presentation.jda.listener.SlashCommandListener;
import greedy.greedybot.presentation.jda.listener.StatusCommandListener;
import greedy.greedybot.scheduledMessage.domain.ScheduledMessage;
import greedy.greedybot.scheduledMessage.domain.ScheduledMessageService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Component
public class ScheduledCommandListener implements SlashCommandListener {

    private final ScheduledMessageService scheduledMessageService;
    private final Logger log = LoggerFactory.getLogger(StatusCommandListener.class);

    public ScheduledCommandListener(ScheduledMessageService scheduledMessageService) {
        this.scheduledMessageService = scheduledMessageService;
    }

    @Override
    public String getCommandName() {
        return "scheduled";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("scheduled", "Schedule a message")
                .addOption(OptionType.STRING, "message", "The message to send", true)
                .addOption(OptionType.STRING, "time", "The time to send (yyyy-MM-dd HH:mm)", true);
    }

    @Override
    public void onAction(@NotNull SlashCommandInteractionEvent event) {
        String message = event.getOption("message").getAsString();
        String timeString = event.getOption("time").getAsString();
        String channelId = event.getChannelId();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        LocalDateTime time;

        //1. 시간 형식 검증
        try {
            time = LocalDateTime.parse(timeString, formatter);
        } catch (DateTimeParseException e) {
            log.error("❌ 잘못된 시간 형식: {}", timeString);
            event.reply("❌ 잘못된 시간 형식입니다! yyyy-MM-dd HH:mm 형식으로 입력해주세요.").setEphemeral(true).queue();
            return;
        }

        //2. 과거 시간 입력 여부 확인
        LocalDateTime now = LocalDateTime.now();
        if (time.isBefore(now)) {
            log.error("❌ 잘못된 예약 시간: {} (과거 시간 입력됨)", timeString);
            event.reply("❌ 예약할 시간은 현재 시간 이후여야 합니다!").setEphemeral(true).queue();
            return;
        }

        log.info("예약된 메시지: {}", message);
        log.info("예약된 시간: {}", time);

        ScheduledMessage scheduledMessage = new ScheduledMessage(message, time, event.getUser().getId(), channelId);
        scheduledMessageService.scheduleMessage(scheduledMessage);

        event.reply("✅ 메시지가 " + timeString + "에 예약되었습니다.").queue();
    }
}
