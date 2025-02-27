package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.application.matching.MatchingService;
import greedy.greedybot.application.matching.dto.MatchingResult;
import greedy.greedybot.common.exception.GreedyBotException;
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ReviewMatchListener implements AutoCompleteInteractionListener {

    private static final Logger log = LoggerFactory.getLogger(ReviewMatchListener.class);
    private static final List<String> reviewees = List.of(
            "BE-1기: 남해윤, 안금서, 신지훈, 정상희, 신혜빈, 김의진, 황승준",
            "FE-1기: 송혜정, 김준수"
    );
    private static final List<String> reviewers =  List.of(
            "BE-1기: 원태연, 이승용, 송은우, 백경환, 김주환, 조승현",
            "FE-1기: 김범수, 김의천"
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

        final String revieweeType = revieweesRawString.substring(0, 4);
        final String reviewerType = reviewersRawString.substring(0, 4);

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

    private List<Command.Choice> setOptions(final List<String> greedyMembers, final CommandAutoCompleteInteractionEvent event) {
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
        return Arrays.stream(rawString.substring(7)
                        .replace(" ", "")
                        .split(","))
                .collect(Collectors.toList());
    }

    private void validateOptions(final OptionMapping optionMission, final OptionMapping optionReviewees, final OptionMapping optionReviewers) {
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
}
