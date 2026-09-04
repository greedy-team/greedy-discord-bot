package greedy.greedybot.presentation.jda.listener.study;

import greedy.greedybot.application.study.StudyGroupService;
import greedy.greedybot.domain.study.StudyGroup;
import greedy.greedybot.domain.study.StudyRole;
import greedy.greedybot.presentation.jda.listener.SlashCommandListener;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ListStudyGroupListener implements SlashCommandListener {

    private static final Logger log = LoggerFactory.getLogger(ListStudyGroupListener.class);

    private static final String ROLE_OPTION = "role";

    private final StudyGroupService studyGroupService;

    public ListStudyGroupListener(final StudyGroupService studyGroupService) {
        this.studyGroupService = studyGroupService;
    }

    @Override
    public String getCommandName() {
        return "study-list";
    }

    @Override
    public SlashCommandData getCommandData() {
        final OptionData role = new OptionData(OptionType.STRING, ROLE_OPTION, "리뷰이 / 리뷰어", false);
        Arrays.stream(StudyRole.values())
            .forEach(studyRole -> role.addChoice(studyRole.label(), studyRole.name()));

        return Commands.slash(this.getCommandName(), "등록된 스터디 그룹 조회")
            .addOptions(role);
    }

    @Override
    public void onAction(@NotNull final SlashCommandInteractionEvent event) {
        final List<StudyGroup> studyGroups = findStudyGroups(event.getOption(ROLE_OPTION));
        log.info("[RECEIVED LIST STUDY GROUP] : {}", studyGroups.size());

        if (studyGroups.isEmpty()) {
            event.reply("등록된 스터디 그룹이 없습니다. `/study-add` 로 등록해주세요.").setEphemeral(true).queue();
            return;
        }

        final String announcement = studyGroups.stream()
            .map(studyGroup -> "- " + studyGroup.describe())
            .collect(Collectors.joining("\n"));
        event.reply("**[등록된 스터디 그룹]**\n" + announcement).setEphemeral(true).queue();
    }

    private List<StudyGroup> findStudyGroups(final OptionMapping optionRole) {
        if (Objects.isNull(optionRole)) {
            return studyGroupService.findAll();
        }
        return studyGroupService.findAllByRole(StudyRole.from(optionRole.getAsString()));
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.values());
    }
}
