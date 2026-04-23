package greedy.greedybot.presentation.jda.listener.message;

import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.presentation.jda.listener.SlashCommandListener;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import greedy.greedybot.presentation.jda.role.ScheduledMessageChannel;
import java.util.Map;
import java.util.Set;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ScheduledMessageCommandListener implements SlashCommandListener {

    private static final Logger log = LoggerFactory.getLogger(ScheduledMessageCommandListener.class);

    private static final Map<String, ScheduledMessageChannel> CHANNEL_NAME_TO_ENUM = Map.of(
        "🚀공통-자유", ScheduledMessageChannel.NOTICE,
        "🍃백엔드-스터디", ScheduledMessageChannel.BACKEND,
        "\uD83E\uDD8B프론트엔드-스터디", ScheduledMessageChannel.FRONT,
        "\uD83E\uDEE7리드-대화", ScheduledMessageChannel.LEAD_CONVERSATION,
        "\uD83C\uDF594기-메인테이너-대화", ScheduledMessageChannel.MAINTAINER_4,
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
        return Commands.slash("scheduled-message", "예약 메세지 등록")
            .addOption(OptionType.STRING, "member", "멘션할 멤버나 그룹이 있다면, 엔터 대신 Space(띄어쓰기)로 구분해 입력하세요. \n"
                + "사용자는 @이름 형태로 입력하세요.", false);
    }

    @Override
    public void onAction(@NotNull SlashCommandInteractionEvent event) {
        try {
            validateAllowedChannel(event);

            String members = null;
            OptionMapping memberOption = event.getOption("member");
            if (memberOption != null) {
                members = memberOption.getAsString();
            }

            String selectMenuId = "scheduled-channel-select";
            if (members != null && !members.isEmpty()) {
                selectMenuId += ":" + members;
            }

            // 채널 선택 드롭다운 생성
            StringSelectMenu.Builder channelMenu = StringSelectMenu.create(selectMenuId)
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

    private void validateAllowedChannel(final @NotNull SlashCommandInteractionEvent event) {
        if (event.getChannel().getIdLong() != allowedChannelId) {
            log.warn("[NOT ALLOWED CHANNEL COMMAND]: {}", event.getUser().getEffectiveName());
            throw new GreedyBotException("예약 메세지는 현재 채널에서 작성할 수 없습니다");
        }
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.LEAD, DiscordRole.DEVELOPER);
    }
}
