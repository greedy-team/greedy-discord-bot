package greedy.greedybot.presentation.jda.listener.matching;

import greedy.greedybot.application.matching.MatchingService;
import greedy.greedybot.application.matching.dto.MatchingResult;
import greedy.greedybot.application.study.StudyGroupService;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.matching.MatchHistory;
import greedy.greedybot.domain.matching.MatchHistoryRepository;
import greedy.greedybot.domain.study.StudyGroup;
import greedy.greedybot.domain.study.StudyRole;
import greedy.greedybot.presentation.jda.listener.AutoCompleteInteractionListener;
import greedy.greedybot.presentation.jda.listener.InCommandButtonInteractionListener;
import greedy.greedybot.presentation.jda.listener.study.StudyGroupChoices;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReviewMatchListener implements AutoCompleteInteractionListener, InCommandButtonInteractionListener {

    private static final Logger log = LoggerFactory.getLogger(ReviewMatchListener.class);

    private static final String MISSION_OPTION = "mission";
    private static final String REVIEWEE_OPTION = "reviewee";
    private static final String REVIEWER_OPTION = "reviewer";
    private static final String REMATCH_BUTTON_ID = "rematch";
    private static final String CONFIRM_BUTTON_ID = "matching-confirm";
    private static final Map<String, List<String>> reviewerSessions = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> revieweeSessions = new ConcurrentHashMap<>();
    private static final Map<String, String> missionNameSession = new ConcurrentHashMap<>();
    private static final Map<String, String> resultSessions = new ConcurrentHashMap<>();
    private static final Map<String, String> revieweeGroupIdSessions = new ConcurrentHashMap<>();
    private static final Map<String, MatchHistory> previousMatchSessions = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> matchedPairSessions = new ConcurrentHashMap<>();

    private final MatchingService matchingService;
    private final StudyGroupService studyGroupService;
    private final MatchHistoryRepository matchHistoryRepository;

    public ReviewMatchListener(
        final MatchingService matchingService,
        final StudyGroupService studyGroupService,
        final MatchHistoryRepository matchHistoryRepository
    ) {
        this.matchingService = matchingService;
        this.studyGroupService = studyGroupService;
        this.matchHistoryRepository = matchHistoryRepository;
    }

    @Override
    public String getCommandName() {
        return "review-match";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(this.getCommandName(), "리뷰어 리뷰이 매칭")
            .addOption(OptionType.STRING, MISSION_OPTION, "미션 이름", true)
            .addOption(OptionType.STRING, REVIEWEE_OPTION, "리뷰이 그룹", true, true)
            .addOption(OptionType.STRING, REVIEWER_OPTION, "리뷰어 그룹", true, true);
    }

    @Override
    public void onAction(@NotNull final SlashCommandInteractionEvent event) {
        final OptionMapping optionMission = event.getOption(MISSION_OPTION);
        final OptionMapping optionReviewees = event.getOption(REVIEWEE_OPTION);
        final OptionMapping optionReviewers = event.getOption(REVIEWER_OPTION);

        validateOptions(optionMission, optionReviewees, optionReviewers);

        final String mission = optionMission.getAsString();
        final StudyGroup revieweeGroup =
            studyGroupService.getByIdAndRole(optionReviewees.getAsString(), StudyRole.REVIEWEE);
        final StudyGroup reviewerGroup =
            studyGroupService.getByIdAndRole(optionReviewers.getAsString(), StudyRole.REVIEWER);

        studyGroupService.validateSameStudy(revieweeGroup, reviewerGroup);

        event.deferReply().setEphemeral(true).queue();
        log.info("[SUCCESS TO GET EVENT]");

        final MatchHistory previousMatch = matchHistoryRepository.findByRevieweeGroupId(revieweeGroup.id())
            .orElseGet(() -> MatchHistory.empty(revieweeGroup.id()));
        log.info("[PREVIOUS MATCH LOADED] : {} ({}건)", revieweeGroup.id(), previousMatch.reviewerByReviewee().size());

        final String matchSessionId = UUID.randomUUID().toString().substring(0, 8);
        missionNameSession.put(matchSessionId, mission);
        reviewerSessions.put(matchSessionId, reviewerGroup.members());
        revieweeSessions.put(matchSessionId, revieweeGroup.members());
        revieweeGroupIdSessions.put(matchSessionId, revieweeGroup.id());
        previousMatchSessions.put(matchSessionId, previousMatch);
        log.info("[MATCHING SESSIONS SAVED] : {}", matchSessionId);

        final String result = match(matchSessionId);
        event.getHook().sendMessage(result)
            .setEphemeral(true)
            .addActionRow(
                Button.primary(REMATCH_BUTTON_ID + ":" + matchSessionId, "\n🔄 재시도"),
                Button.success(CONFIRM_BUTTON_ID + ":" + matchSessionId, "✅ 확정")
            )
            .queue();
    }

    private String match(final String matchSessionId) {
        final String mission = missionNameSession.get(matchSessionId);
        final List<String> reviewees = revieweeSessions.get(matchSessionId);
        final List<String> reviewers = reviewerSessions.get(matchSessionId);
        if (Objects.isNull(reviewees) || Objects.isNull(reviewers) || mission.isBlank()) {
            log.warn("[REVIEWER OR REVIEWEE SESSIONS NOT FOUND]");
            throw new GreedyBotException("🚫 리뷰어 또는 리뷰이 세션이 존재하지 않습니다. 다시 시도해주세요.");
        }

        log.info("[START MATCHING] : {}", mission);
        final MatchHistory previousMatch = previousMatchSessions.getOrDefault(
            matchSessionId, MatchHistory.empty(matchSessionId));
        final MatchingResult matchingResultAnnouncement =
            matchingService.matchStudy(reviewees, reviewers, previousMatch);
        final String result =
            "[**" + mission + "** 리뷰어 매칭 결과]\n\n" + matchingResultAnnouncement.toDiscordAnnouncement();
        resultSessions.put(matchSessionId, result);
        matchedPairSessions.put(matchSessionId, matchingResultAnnouncement.toReviewerByReviewee());
        return result;
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.LEAD, DiscordRole.DEVELOPER);
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull final CommandAutoCompleteInteractionEvent event) {
        final String focusedOptionName = event.getFocusedOption().getName();
        final String focusedValue = event.getFocusedOption().getValue();

        if (REVIEWEE_OPTION.equals(focusedOptionName)) {
            event.replyChoices(
                StudyGroupChoices.from(studyGroupService.findAllByRole(StudyRole.REVIEWEE), focusedValue)
            ).queue();
            log.info("[SUCCESS TO GET REVIEWEE OPTIONS]");
            return;
        }

        if (REVIEWER_OPTION.equals(focusedOptionName)) {
            event.replyChoices(
                StudyGroupChoices.from(studyGroupService.findAllByRole(StudyRole.REVIEWER), focusedValue)
            ).queue();
            log.info("[SUCCESS TO GET REVIEWER OPTIONS]");
        }
    }

    private void validateOptions(final OptionMapping optionMission, final OptionMapping optionReviewees,
        final OptionMapping optionReviewers) {
        if (Objects.isNull(optionMission)) {
            log.warn("[EMPTY MISSION]");
            throw new GreedyBotException("🚫 미션 정보가 입력 되지 않았습니다.");
        }

        if (Objects.isNull(optionReviewees)) {
            log.warn("[EMPTY REVIEWEES]");
            throw new GreedyBotException("🚫 리뷰이 정보가 입력 되지 않았습니다.");
        }

        if (Objects.isNull(optionReviewers)) {
            log.warn("[EMPTY REVIEWERS]");
            throw new GreedyBotException("🚫 리뷰어 정보가 입력 되지 않았습니다.");
        }
    }

    @Override
    public void onButtonInteraction(final ButtonInteractionEvent event) {
        final String[] buttonIdAndMatchSessionId = event.getComponentId().split(":");
        final String buttonId = buttonIdAndMatchSessionId[0];
        final String matchSessionId = buttonIdAndMatchSessionId[1];

        if (buttonId.equals(REMATCH_BUTTON_ID)) {
            log.info("[RETRY MATCHING]");
            final String result = match(matchSessionId);
            event.editMessage(result).setActionRow(
                Button.primary(REMATCH_BUTTON_ID + ":" + matchSessionId, "🔄 재시도"),
                Button.success(CONFIRM_BUTTON_ID + ":" + matchSessionId, "✅ 확정")
            ).queue();
            resultSessions.put(matchSessionId, result);
            return;
        }

        if (buttonId.equals(CONFIRM_BUTTON_ID)) {
            log.info("[CONFIRM MATCHING]");
            final String result = resultSessions.get(matchSessionId);
            if (Objects.isNull(result)) {
                log.warn("[RESULT SESSION NOT FOUND]");
                event.reply("❌ 리뷰어 리뷰이 매칭 결과가 존재하지 않습니다. 다시 시도해주세요.").setEphemeral(true).queue();
                return;
            }
            saveMatchHistory(matchSessionId);
            event.editMessage("✅ **매칭 확정!**\n결과를 채널에 공개적으로 전송했습니다.")
                .setComponents()
                .queue();
            clearSession(matchSessionId);
            event.getChannel().sendMessage(result).queue();
            return;
        }

        log.warn("[UNSUPPORTED BUTTON COMMAND]: {}", buttonId);
    }

    // 확정된 매칭만 다음 매칭의 비교 대상이 된다
    private void saveMatchHistory(final String matchSessionId) {
        final String revieweeGroupId = revieweeGroupIdSessions.get(matchSessionId);
        final Map<String, String> matchedPairs = matchedPairSessions.get(matchSessionId);
        if (Objects.isNull(revieweeGroupId) || Objects.isNull(matchedPairs)) {
            log.warn("[MATCH HISTORY SESSION NOT FOUND] : {}", matchSessionId);
            return;
        }
        final String mission = missionNameSession.getOrDefault(matchSessionId, "");
        matchHistoryRepository.saveMatchHistory(new MatchHistory(revieweeGroupId, mission, matchedPairs));
        log.info("[MATCH HISTORY SAVED] : {}", revieweeGroupId);
    }

    private void clearSession(final String matchSessionId) {
        reviewerSessions.remove(matchSessionId);
        revieweeSessions.remove(matchSessionId);
        missionNameSession.remove(matchSessionId);
        resultSessions.remove(matchSessionId);
        revieweeGroupIdSessions.remove(matchSessionId);
        previousMatchSessions.remove(matchSessionId);
        matchedPairSessions.remove(matchSessionId);
    }

    @Override
    public boolean isSupportingButtonId(String buttonId) {
        return buttonId.startsWith(REMATCH_BUTTON_ID) || buttonId.startsWith(CONFIRM_BUTTON_ID);
    }
}
