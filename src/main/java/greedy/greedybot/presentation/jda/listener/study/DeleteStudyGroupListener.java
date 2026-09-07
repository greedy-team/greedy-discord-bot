package greedy.greedybot.presentation.jda.listener.study;

import greedy.greedybot.application.study.StudyGroupService;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.study.StudyGroup;
import greedy.greedybot.presentation.jda.listener.AutoCompleteInteractionListener;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import java.util.Objects;
import java.util.Set;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeleteStudyGroupListener implements AutoCompleteInteractionListener {

    private static final Logger log = LoggerFactory.getLogger(DeleteStudyGroupListener.class);

    private static final String ID_OPTION = "id";

    private final StudyGroupService studyGroupService;

    public DeleteStudyGroupListener(final StudyGroupService studyGroupService) {
        this.studyGroupService = studyGroupService;
    }

    @Override
    public String getCommandName() {
        return "study-delete";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(this.getCommandName(), "스터디 그룹 삭제")
            .addOption(OptionType.STRING, ID_OPTION, "삭제할 그룹", true, true);
    }

    @Override
    public void onAction(@NotNull final SlashCommandInteractionEvent event) {
        final OptionMapping optionId = event.getOption(ID_OPTION);
        if (Objects.isNull(optionId)) {
            log.warn("[EMPTY STUDY GROUP ID]");
            throw new GreedyBotException("🚫 삭제할 스터디 그룹이 입력 되지 않았습니다.");
        }
        final String id = optionId.getAsString();
        log.info("[RECEIVED DELETE STUDY GROUP] : {}", id);

        final StudyGroup studyGroup = studyGroupService.deleteStudyGroup(id);
        event.reply("""
            ✅ 스터디 그룹을 삭제했어요.
            - %s
            """.formatted(studyGroup.describe()))
            .setEphemeral(true)
            .queue();
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull final CommandAutoCompleteInteractionEvent event) {
        if (!event.getFocusedOption().getName().equals(ID_OPTION)) {
            return;
        }
        event.replyChoices(
            StudyGroupChoices.from(studyGroupService.findAll(), event.getFocusedOption().getValue())
        ).queue();
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.LEAD, DiscordRole.DEVELOPER);
    }
}
