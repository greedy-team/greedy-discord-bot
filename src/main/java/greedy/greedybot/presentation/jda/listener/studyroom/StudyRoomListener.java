package greedy.greedybot.presentation.jda.listener.studyroom;

import greedy.greedybot.presentation.jda.listener.SlashCommandListener;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import java.util.Set;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StudyRoomListener implements SlashCommandListener {

    @Value("${study_room.site}")
    private String studyRoomSiteUrl;

    @Override
    public String getCommandName() {
        return "study-room";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(this.getCommandName(), "버튼을 누르면 스터디룸 예약 자동화 사이트로 이동합니다.");
    }

    @Override
    public void onAction(@NotNull final SlashCommandInteractionEvent event) {
        event.reply(" ")
            .addActionRow(
                Button.link(studyRoomSiteUrl, "🚀 스터디룸 예약 바로가기")
            )
            .setEphemeral(true)
            .queue();
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.DEVELOPER, DiscordRole.LEAD);
    }
}
