package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import greedy.greedybot.presentation.jda.role.ScheduledMessageChannel;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ScheduledCommandListener implements AutoCompleteInteractionListener {

    private static final Logger log = LoggerFactory.getLogger(ScheduledCommandListener.class);

    private static final Map<String, ScheduledMessageChannel> CHANNEL_NAME_TO_ENUM = Map.of(
        "🚀공통-자유", ScheduledMessageChannel.NOTICE,
        "🍃백엔드-스터디", ScheduledMessageChannel.BACKEND,
        "\uD83E\uDD8B프론트엔드-스터디", ScheduledMessageChannel.FRONT,
        "\uD83E\uDEE7리드-대화", ScheduledMessageChannel.LEAD_CONVERSATION,
        "tf-discord-test-ground", ScheduledMessageChannel.TEST
    );

    @Value("${discord.message_writing_channel}")
    private Long allowedChannelId;

    @Override
    public String getCommandName() {
        return "scheduled-message";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash("scheduled-message", "Schedule a message");
    }

    @Override
    public void onAction(@NotNull SlashCommandInteractionEvent event) {
        try {
            validateAllowedChannel(event);

            // 채널 선택 드롭다운 생성
            StringSelectMenu.Builder channelMenu = StringSelectMenu.create("scheduled-channel-modal")
                .setPlaceholder("메시지를 보낼 채널을 선택하세요");

            CHANNEL_NAME_TO_ENUM.forEach((name, enumValue) -> {
                channelMenu.addOption(name, enumValue.name());
            });

            event.reply("📌 메시지를 보낼 채널을 선택하세요:")
                .addActionRow(channelMenu.build())
                .setEphemeral(true)
                .queue();
        } catch (GreedyBotException e) {
            log.error(e.getMessage());
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull final CommandAutoCompleteInteractionEvent event) {
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

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.LEAD);
    }
}
