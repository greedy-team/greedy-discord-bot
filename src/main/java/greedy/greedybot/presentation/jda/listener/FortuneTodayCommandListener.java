package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.application.fortune.FortuneService;
import java.time.LocalDate;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FortuneTodayCommandListener implements SlashCommandListener {

    private static final Logger log = LoggerFactory.getLogger(FortuneTodayCommandListener.class);

    private final FortuneService fortuneService;

    public FortuneTodayCommandListener(final FortuneService fortuneService) {
        this.fortuneService = fortuneService;
    }

    @Override
    public String getCommandName() {
        return "fortune-today";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(this.getCommandName(), "오늘의 운세를 알려줍니다. 운세는 하루 단위로 달라집니다!");
    }

    @Override
    public void onAction(@NotNull final SlashCommandInteractionEvent event) {
        final User reuqestUser = event.getUser();
        final Long userId = reuqestUser.getIdLong();
        final String nickname = reuqestUser.getEffectiveName();
        log.info("[RECEIVED FORTUNE TODAY] : userId={}, nickname={}", userId, nickname);

        final LocalDate today = LocalDate.now();
        event.deferReply().queue();
        final String result = fortuneService.findTodayFortuneByKey(userId, today);
        event.getHook().sendMessage("""
                **%s**님의 오늘의 운세!
                %s
                """.formatted(nickname, result)).queue();
    }
}
