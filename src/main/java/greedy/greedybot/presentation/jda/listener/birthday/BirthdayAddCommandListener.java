
package greedy.greedybot.presentation.jda.listener.birthday;

import greedy.greedybot.application.birthday.BirthdayService;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.birthday.Birthday;
import greedy.greedybot.presentation.jda.listener.SlashCommandListener;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

@Component
public class BirthdayAddCommandListener implements SlashCommandListener {

    private static final Logger log = LoggerFactory.getLogger(BirthdayAddCommandListener.class);

    private static final DateTimeFormatter MM_DD = DateTimeFormatter.ofPattern("MM-dd");

    private final BirthdayService birthdayService;

    public BirthdayAddCommandListener(BirthdayService birthdayService) {
        this.birthdayService = birthdayService;
    }

    @Override
    public String getCommandName() {
        return "birthday-add";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getCommandName(), "생일 등록")
                .addOption(OptionType.STRING, "userid", "디스코드 아이디", true)
                .addOption(OptionType.STRING, "username", "이름", true)
                .addOption(OptionType.STRING, "birthday", "생일은 MM-DD형식으로 입력해주세요", true);

    }

    @Override
    public void onAction(@NotNull SlashCommandInteractionEvent event) {
        try {
            OptionMapping idOpt = event.getOption("userid");
            OptionMapping nameOpt = event.getOption("username");
            OptionMapping dateOpt = event.getOption("birthday");

            if (idOpt == null || nameOpt == null || dateOpt == null) {
                event.reply("아이디, 이름, 생일을 다시 확인해주세요!").setEphemeral(true).queue();
                return;
            }

            String userId = idOpt.getAsString().trim();
            String userName = nameOpt.getAsString().trim();
            String stringDate = dateOpt.getAsString().trim();

            MonthDay date;
            try {
                date = MonthDay.parse(stringDate, MM_DD);
            } catch (DateTimeParseException e) {
                log.error("날짜 형식 오류", e);
                event.reply("날짜 형식이 잘못되었습니다!").setEphemeral(true).queue();
                return;
            }
            Birthday birthday = new Birthday(userId, userName, date);
            birthdayService.register(birthday);
            log.info("생일 등록 성공 Id:{} Name:{} Date:{}", userId, userName, stringDate);

            event.reply("생일 등록 완료! [" + userName + "/" + stringDate + "]").queue();

        } catch (GreedyBotException e) {
            log.error("생일 등록 실패", e);
            event.reply("생일 등록 실패!" + e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.DEVELOPER);
    }
}
