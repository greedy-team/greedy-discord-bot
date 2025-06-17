package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.domain.message.ScheduledMessage;
import greedy.greedybot.application.message.ScheduledMessageService;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.presentation.jda.role.DiscordRole;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import greedy.greedybot.presentation.jda.role.ScheduledMessageChannel;
import greedy.greedybot.presentation.jda.role.ScheduledMessageChannels;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class ScheduledCommandListener implements AutoCompleteInteractionListener {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH);
    private static final Logger log = LoggerFactory.getLogger(ScheduledCommandListener.class);

    private static final Map<String, ScheduledMessageChannel> CHANNEL_NAME_TO_ENUM = Map.of(
            "🚀공통-자유", ScheduledMessageChannel.NOTICE,
            "🍃백엔드-스터디", ScheduledMessageChannel.BACKEND,
            "\uD83E\uDD8B프론트엔드-스터디", ScheduledMessageChannel.FRONT,
            "\uD83E\uDEE7리드-대화", ScheduledMessageChannel.LEAD_CONVERSATION,
            "tf-discord-test-ground", ScheduledMessageChannel.TEST
    );

    private final ScheduledMessageService scheduledMessageService;
    private final ScheduledMessageChannels scheduledMessageChannels;

    @Value("${discord.message_writing_channel}")
    private Long allowedChannelId;

    public ScheduledCommandListener(ScheduledMessageService scheduledMessageService, ScheduledMessageChannels scheduledMessageChannels) {
        this.scheduledMessageService = scheduledMessageService;
        this.scheduledMessageChannels = scheduledMessageChannels;
    }

    @Override
    public String getCommandName() {
        return "add-scheduled-message";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("add-scheduled-message", "Schedule a message")
                .addOption(OptionType.STRING, "message", "The message to send", true)
                .addOption(OptionType.STRING, "time", "The time to send (yyyy-MM-dd HH:mm)", true)
                .addOption(OptionType.STRING, "channel", "The channel to send", true, true);
    }

    @Override
    public void onAction(final @NotNull SlashCommandInteractionEvent event) {
        try {
            validateAllowedChannel(event);

            final String message = event.getOption("message").getAsString();
            final String timeString = event.getOption("time").getAsString();
            final String channelId = event.getOption("channel").getAsString();

            // 시간 관련 검증
            LocalDateTime time = parseScheduledTime(timeString); //형식 검증
            isValidScheduledTime(time);//과거 시간 입력 검증

            final ScheduledMessageChannel selectedEnum = CHANNEL_NAME_TO_ENUM.get(channelId);
            final long resolvedChannelId = scheduledMessageChannels.getChannelId(selectedEnum);

            event.deferReply().queue();

            final ScheduledMessage scheduledMessage = new ScheduledMessage(message, time, event.getUser().getId(), String.valueOf(resolvedChannelId));
            scheduledMessageService.scheduleMessage(scheduledMessage);

            log.info("✅ 메시지가 "+ channelId+" 채널에 예약되었습니다.");

            event.getHook().sendMessage("✅ 메시지가 " + timeString + "에 예약되었습니다.").queue();
        } catch (GreedyBotException e) {
            log.error(e.getMessage());
            event.getHook().sendMessage(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull final CommandAutoCompleteInteractionEvent event){
        List<Command.Choice> options = CHANNEL_NAME_TO_ENUM.keySet().stream()
                .filter(name -> name.startsWith(event.getFocusedOption().getValue()))
                .map(name -> new Command.Choice(name, name))
                .collect(Collectors.toList());

        event.replyChoices(options).queue();
        log.info("[SUCCESS TO GET RECEIVING CHANNEL]");
    }


    private void validateAllowedChannel(final @NotNull SlashCommandInteractionEvent event) {
        if (event.getChannel().getIdLong() != allowedChannelId) {
            log.warn("[NOT ALLOWED CHANNEL COMMAND]: {}", event.getUser().getEffectiveName());
            throw new GreedyBotException("예약 메세지는 현재 채널에서 작성할 수 없습니다");
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
