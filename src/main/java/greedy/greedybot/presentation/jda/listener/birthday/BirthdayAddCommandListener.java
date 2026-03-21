package greedy.greedybot.presentation.jda.listener.birthday;

import greedy.greedybot.application.birthday.BirthdayRequestService;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.birthday.Birthday;
import greedy.greedybot.domain.birthday.BirthdayReason;
import greedy.greedybot.presentation.jda.listener.SlashCommandListener;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

@Component
public class BirthdayAddCommandListener implements SlashCommandListener {

    private static final Logger log = LoggerFactory.getLogger(BirthdayAddCommandListener.class);

    private static final DateTimeFormatter MM_DD = DateTimeFormatter.ofPattern("MM-dd");

    private final BirthdayRequestService birthdayRequestService;

    public BirthdayAddCommandListener(BirthdayRequestService birthdayRequestService) {
        this.birthdayRequestService = birthdayRequestService;
    }

    @Override
    public String getCommandName() {
        return "birthday-add";
    }

    @Override
    public SlashCommandData getCommandData() {
        OptionData reasonOption = new OptionData(OptionType.STRING, "reason", "등록 사유를 선택해주세요.", true)
                .addChoice("최초 등록", "FEAT")
                .addChoice("생일 잘못 입력", "FIX");
        return Commands.slash(getCommandName(), "생일 등록")
                .addOption(OptionType.STRING, "birthday", "생일은 MM-DD 형식으로 입력해주세요. (예시: 04-19)", true)
                .addOptions(reasonOption);
    }

    @Override
    public void onAction(@NotNull SlashCommandInteractionEvent event) {
        try {
            OptionMapping dateOpt = event.getOption("birthday");
            OptionMapping reasonOpt = event.getOption("reason");
            BirthdayReason reason = BirthdayReason.valueOf(reasonOpt.getAsString());
            if (dateOpt == null || reasonOpt == null) {
                event.reply("생일을 다시 확인해주세요!").setEphemeral(true).queue();
                return;
            }

            String stringDate = dateOpt.getAsString().trim();
            String userId = event.getUser().getId();
            String userName = event.getMember() != null
                    ? event.getMember().getEffectiveName()
                    : event.getUser().getName();
            MonthDay date;
            try {

                date = MonthDay.parse(stringDate, MM_DD);
            } catch (DateTimeParseException e) {
                log.warn("날짜 형식 오류: userId={}, userName={}, input={}", userId, userName, stringDate);
                event.reply("날짜 형식이 잘못되었습니다!").setEphemeral(true).queue();
                return;
            }

            Birthday birthday = new Birthday(userId, userName, date);
            String mmdd = date.format(MM_DD);
            birthdayRequestService.submitRequest(userId, mmdd, reason.getLabel(), userName);
            log.info("생일 등록 성공 userId={}, userName={}, input={}", userId, userName, stringDate);

            event.reply("생일 등록 완료! [" + userName + ":" + stringDate + "]").setEphemeral(true).queue();

        } catch (GreedyBotException e) {
            log.error("생일 등록 실패", e);
            event.reply("생일 등록 실패!" + e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.MEMBER);
    }
}
