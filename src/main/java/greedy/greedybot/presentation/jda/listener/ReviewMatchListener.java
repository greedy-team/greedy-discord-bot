package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.domain.matching.*;
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
    private static final String[] reviewees = new String[]{
            "BE-1기: 남해윤, 안금서, 신지훈, 정상희, 신혜빈, 김의진, 황승준",
            "FE-1기: 송혜정, 김준수",
    };
    private static final String[] reviewers = new String[]{
            "BE-1기: 원태연, 이승용, 송은우, 백경환, 김주환, 조승현",
            "FE-1기: 김범수, 김의천",
    };


    private final MatchingService matchingService;


    public ReviewMatchListener(
            final MatchingService matchingService
    ) {
        this.matchingService = matchingService;
    }

    @Override
    public String getCommandName() {
        return "review";
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

        if (Objects.isNull(optionMission)) {
            log.warn("EMPTY MISSION");
            event.reply("EMPTY MISSION").queue();
            return;
        }

        if (Objects.isNull(optionReviewees)) {
            log.warn("EMPTY REVIEWEES");
            event.reply("EMPTY REVIEWEE").queue();
            return;
        }

        if (Objects.isNull(optionReviewers)) {
            log.warn("EMPTY REVIEWERS");
            event.reply("EMPTY REVIEWERS").queue();
            return;
        }

        final String mission = optionMission.getAsString();
        final String revieweesRawString = optionReviewees.getAsString();
        final String reviewersRawString = optionReviewers.getAsString();

        final String revieweeType = revieweesRawString.substring(0, 4);
        final String reviewerType = reviewersRawString.substring(0, 4);

        if (!revieweeType.equals(reviewerType)) {
            log.warn("MATCH TYPE DISMATCH");
            event.reply("MATCH TYPE DISMATCH").queue();
            return;
        }

        event.deferReply().queue();
        log.info("SUCCESS TO GET EVENT");

        final List<String> reviewees = extractNamesFromRawString(revieweesRawString);
        final List<String> reviewers = extractNamesFromRawString(reviewersRawString);


        String matchingResult = matchingService.matchStudy(reviewees, reviewers);

        log.info("MATCH SUCCESS");

        String message = "[**" + mission + "** 리뷰어 매칭 결과]\n\n" + matchingResult;
        event.getHook().sendMessage(message).queue();
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull final CommandAutoCompleteInteractionEvent event) {
        if (isRevieweeAutoCompleteEvent(event)) {
            List<Command.Choice> options = setOptions(reviewees, event);
            event.replyChoices(options).queue();
            log.info("SUCCESS TO GET REVIEWEE OPTIONS");
        }

        if (isReviewerAutoCompleteEvent(event)) {
            List<Command.Choice> options = setOptions(reviewers, event);
            event.replyChoices(options).queue();
            log.info("SUCCESS TO GET REVIEWER OPTIONS");
        }
    }

    private List<Command.Choice> setOptions(final String[] greedyMembers, final CommandAutoCompleteInteractionEvent event) {
        return Stream.of(greedyMembers)
                .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // 사용자의 입력과 일치하는 단어만 표시
                .map(word -> new Command.Choice(word, word)) // 단어를 선택지로 변환
                .collect(Collectors.toList());
    }

    private boolean isRevieweeAutoCompleteEvent(final CommandAutoCompleteInteractionEvent event) {
        return event.getName().equals("review") && event.getFocusedOption().getName().equals("reviewee");

    }

    private boolean isReviewerAutoCompleteEvent(final CommandAutoCompleteInteractionEvent event) {
        return event.getName().equals("review") && event.getFocusedOption().getName().equals("reviewer");
    }

    private List<String> extractNamesFromRawString(String rawString) {
        return Arrays.stream(rawString.substring(7)
                        .replace(" ", "")
                        .split(","))
                .collect(Collectors.toList());
    }
}
