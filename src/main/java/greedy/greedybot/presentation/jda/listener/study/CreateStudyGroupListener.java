package greedy.greedybot.presentation.jda.listener.study;

import greedy.greedybot.application.study.StudyGroupService;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.study.StudyGroup;
import greedy.greedybot.domain.study.StudyRole;
import greedy.greedybot.domain.study.StudyType;
import greedy.greedybot.presentation.jda.listener.SlashCommandListener;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
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
public class CreateStudyGroupListener implements SlashCommandListener {

    private static final Logger log = LoggerFactory.getLogger(CreateStudyGroupListener.class);

    private static final String ID_OPTION = "id";
    private static final String ROLE_OPTION = "role";
    private static final String TYPE_OPTION = "type";
    private static final String GENERATION_OPTION = "generation";
    private static final String MEMBERS_OPTION = "members";

    private final StudyGroupService studyGroupService;

    public CreateStudyGroupListener(final StudyGroupService studyGroupService) {
        this.studyGroupService = studyGroupService;
    }

    @Override
    public String getCommandName() {
        return "study-add";
    }

    @Override
    public SlashCommandData getCommandData() {
        final OptionData role = new OptionData(OptionType.STRING, ROLE_OPTION, "리뷰이 / 리뷰어", true);
        Arrays.stream(StudyRole.values())
            .forEach(studyRole -> role.addChoice(studyRole.label(), studyRole.name()));

        final OptionData type = new OptionData(OptionType.STRING, TYPE_OPTION, "프론트엔드 / 백엔드", true);
        Arrays.stream(StudyType.values())
            .forEach(studyType -> type.addChoice(studyType.label(), studyType.name()));

        return Commands.slash(this.getCommandName(), "스터디 그룹 등록")
            .addOption(OptionType.STRING, ID_OPTION, "그룹 id (ex. be-4, be-4-java)", true)
            .addOptions(role, type)
            .addOption(OptionType.INTEGER, GENERATION_OPTION, "기수 (숫자만, ex. 4)", true)
            .addOption(OptionType.STRING, MEMBERS_OPTION, "멤버 이름 (쉼표로 구분)", true);
    }

    @Override
    public void onAction(@NotNull final SlashCommandInteractionEvent event) {
        final String id = getRequiredOption(event, ID_OPTION).getAsString();
        final StudyRole role = StudyRole.from(getRequiredOption(event, ROLE_OPTION).getAsString());
        final StudyType type = StudyType.from(getRequiredOption(event, TYPE_OPTION).getAsString());
        final int generation = getRequiredOption(event, GENERATION_OPTION).getAsInt();
        final String members = getRequiredOption(event, MEMBERS_OPTION).getAsString();
        log.info("[RECEIVED ADD STUDY GROUP] : {}", id);

        final StudyGroup studyGroup = studyGroupService.createStudyGroup(id, role, type, generation, members);
        event.reply("""
            ✅ 스터디 그룹을 등록했어요!
            - %s
            """.formatted(studyGroup.describe()))
            .setEphemeral(true)
            .queue();
    }

    private OptionMapping getRequiredOption(final SlashCommandInteractionEvent event, final String optionName) {
        final OptionMapping option = event.getOption(optionName);
        if (Objects.isNull(option)) {
            log.warn("[EMPTY STUDY GROUP OPTION] : {}", optionName);
            throw new GreedyBotException("🚫 " + optionName + " 정보가 입력 되지 않았습니다.");
        }
        return option;
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.LEAD, DiscordRole.DEVELOPER);
    }
}
