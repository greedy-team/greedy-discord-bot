package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.application.matching.MatchingService;
import greedy.greedybot.application.matching.dto.MatchingResult;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReviewMatchListener implements AutoCompleteInteractionListener {

    private static final Logger log = LoggerFactory.getLogger(ReviewMatchListener.class);
    private static final List<String> reviewees = List.of(
            "BE-2기: 김지우, 이창희, 황혜림, 전서희, 허석준, 염지환",
            "FE-2기: 강동현, 신지훈, 신지우, 박찬빈, 임규영, 정창우"
    );
    private static final List<String> reviewers = List.of(
            "BE-2기: 원태연, 백경환, 송은우, 조승현, 정다빈",
            "FE-2기: 김범수, 김의천, 송혜정, 김민석"
    );


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

        event.deferReply().queue();
        log.info("[SUCCESS TO GET EVENT]");

        final List<String> reviewees = extractNamesFromRawString(revieweesRawString);
        final List<String> reviewers = extractNamesFromRawString(reviewersRawString);

        MatchingResult matchingResultAnnouncement = matchingService.matchStudy(reviewees, reviewers);

        log.info("[MATCH SUCCESS]");

        String message = "[**" + mission + "** 리뷰어 매칭 결과]\n\n" + matchingResultAnnouncement.toDiscordAnnouncement();
        event.getHook().sendMessage(message).queue();
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.LEAD);
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
}
