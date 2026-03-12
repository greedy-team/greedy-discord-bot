package greedy.greedybot.presentation.jda.listener.matching;

import greedy.greedybot.application.matching.MatchingService;
import greedy.greedybot.application.matching.dto.MatchingResult;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.presentation.jda.listener.AutoCompleteInteractionListener;
import greedy.greedybot.presentation.jda.listener.InCommandButtonInteractionListener;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
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
    private static final List<String> reviewees = List.of(
        "BE-4기: 이태규, 김민욱, 이채현, 정명준, 김하은, 강대현",
        "FE-4기: 천동현, 김동건, 홍의민, 고규민"
    );
    private static final List<String> reviewers = List.of(
        "BE-4기(Java): 정다빈, 조상준, 이진, 이창희, 남해윤, 하수한",
        "BE-4기(Spring): 정다빈, 조상준, 이진, 김민기, 김수민, 신혜빈",
        "FE-4기(1차): 정창우, 김의천, 임규영, 송혜정",
        "FE-4기(2차): 김범수, 김의천, 임규영, 송혜정",
        "FE-4기(3차): 박찬빈, 김의천, 임규영, 송혜정"
    );
    private static final String REMATCH_BUTTON_ID = "rematch";
    private static final String CONFIRM_BUTTON_ID = "matching-confirm";
    private static final Map<String, List<String>> reviewerSessions = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> revieweeSessions = new ConcurrentHashMap<>();
    private static final Map<String, String> missionNameSession = new ConcurrentHashMap<>();
    private static final Map<String, String> resultSessions = new ConcurrentHashMap<>();

    private final MatchingService matchingService;

    public ReviewMatchListener(
        final MatchingService matchingService
    ) {
        this.matchingService = matchingService;
    }

    @Override
    public String getCommandName() {
        return "review-match";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(this.getCommandName(), "리뷰어 리뷰이 매칭")
            .addOption(OptionType.STRING, "mission", "미션 이름", true)
            .addOption(OptionType.STRING, "reviewee", "리뷰이", true, true)
            .addOption(OptionType.STRING, "reviewer", "리뷰어", true, true);
    }

    @Override
    public void onAction(@NotNull final SlashCommandInteractionEvent event) {
        final OptionMapping optionMission = event.getOption("mission");
        final OptionMapping optionReviewees = event.getOption("reviewee");
        final OptionMapping optionReviewers = event.getOption("reviewer");

        validateOptions(optionMission, optionReviewees, optionReviewers);

        final String mission = optionMission.getAsString();
        final String revieweesRawString = optionReviewees.getAsString();
        final String reviewersRawString = optionReviewers.getAsString();

        final String revieweeType = getStudyType(revieweesRawString);
        final String reviewerType = getStudyType(reviewersRawString);

        validateReviewerAndRevieweeType(revieweeType, reviewerType);

        event.deferReply().setEphemeral(true).queue();
        log.info("[SUCCESS TO GET EVENT]");
        final List<String> reviewees = extractNamesFromRawString(revieweesRawString);
        final List<String> reviewers = extractNamesFromRawString(reviewersRawString);

        final String matchSessionId = UUID.randomUUID().toString().substring(0, 8);
        missionNameSession.put(matchSessionId, mission);
        reviewerSessions.put(matchSessionId, reviewers);
        revieweeSessions.put(matchSessionId, reviewees);
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
            throw new GreedyBotException("\uD83D\uDEAB 리뷰어 또는 리뷰이 세션이 존재하지 않습니다. 다시 시도해주세요.");
        }

        log.info("[START MATCHING] : {}", mission);
        final MatchingResult matchingResultAnnouncement = matchingService.matchStudy(reviewees, reviewers);
        final String result =
            "[**" + mission + "** 리뷰어 매칭 결과]\n\n" + matchingResultAnnouncement.toDiscordAnnouncement();
        resultSessions.put(matchSessionId, result);
        return result;
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.LEAD, DiscordRole.DEVELOPER);
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull final CommandAutoCompleteInteractionEvent event) {
        if (isRevieweeAutoCompleteEvent(event)) {
            List<Command.Choice> options = setOptions(reviewees, event);
            event.replyChoices(options).queue();
            log.info("[SUCCESS TO GET REVIEWEE OPTIONS]");
        }

        if (isReviewerAutoCompleteEvent(event)) {
            List<Command.Choice> options = setOptions(reviewers, event);
            event.replyChoices(options).queue();
            log.info("[SUCCESS TO GET REVIEWER OPTIONS]");
        }
    }

    private List<Command.Choice> setOptions(final List<String> greedyMembers,
        final CommandAutoCompleteInteractionEvent event) {
        return greedyMembers.stream()
            .filter(member -> member.startsWith(event.getFocusedOption().getValue()))
            .map(member -> new Command.Choice(member, member))
            .collect(Collectors.toList());
    }

    private boolean isRevieweeAutoCompleteEvent(final CommandAutoCompleteInteractionEvent event) {
        return event.getName().equals("review-match") && event.getFocusedOption().getName().equals("reviewee");

    }

    private boolean isReviewerAutoCompleteEvent(final CommandAutoCompleteInteractionEvent event) {
        return event.getName().equals("review-match") && event.getFocusedOption().getName().equals("reviewer");
    }

    private List<String> extractNamesFromRawString(String rawString) {
        return Arrays.stream(rawString
                .split(":")[1]
                .trim()
                .split(","))
            .map(String::trim)
            .collect(Collectors.toList());
    }

    private void validateOptions(final OptionMapping optionMission, final OptionMapping optionReviewees,
        final OptionMapping optionReviewers) {
        if (Objects.isNull(optionMission)) {
            log.warn("[EMPTY MISSION]");
            throw new GreedyBotException("\uD83D\uDEAB 미션 정보가 입력 되지 않았습니다.");
        }

        if (Objects.isNull(optionReviewees)) {
            log.warn("[EMPTY REVIEWEES]");
            throw new GreedyBotException("\uD83D\uDEAB 리뷰이 정보가 입력 되지 않았습니다.");
        }

        if (Objects.isNull(optionReviewers)) {
            log.warn("[EMPTY REVIEWERS]");
            throw new GreedyBotException("\uD83D\uDEAB 리뷰어 정보가 입력 되지 않았습니다.");
        }
    }

    private void validateReviewerAndRevieweeType(final String revieweeType, final String reviewerType) {
        if (!revieweeType.equals(reviewerType)) {
            log.warn("[REVIEWER AND REVIEWEE STUDY TYPE DISMATCH]");
            throw new GreedyBotException("\uD83D\uDEAB 리뷰어 리뷰이 스터디 타입 정보가 일치 하지 않습니다.");
        }
    }

    private String getStudyType(final String groupInfo) {
        return groupInfo.split(":")[0];
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
            }
            event.editMessage("✅ **매칭 확정!**\n결과를 채널에 공개적으로 전송했습니다.")
                .setComponents()
                .queue();
            clearSession(matchSessionId);
            event.getChannel().sendMessage(result).queue();
            return;
        }

        log.warn("[UNSUPPORTED BUTTON COMMAND]: {}", buttonId);
    }

    private void clearSession(final String matchSessionId) {
        reviewerSessions.remove(matchSessionId);
        revieweeSessions.remove(matchSessionId);
        missionNameSession.remove(matchSessionId);
        resultSessions.remove(matchSessionId);
    }

    @Override
    public boolean isSupportingButtonId(String buttonId) {
        return buttonId.startsWith(REMATCH_BUTTON_ID) || buttonId.startsWith(CONFIRM_BUTTON_ID);
    }
}
