package greedy.greedybot.presentation.jda.listener.birthday;

import greedy.greedybot.application.birthday.BirthdayService;
import greedy.greedybot.domain.birthday.Birthday;
import greedy.greedybot.presentation.jda.listener.SlashCommandListener;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class BirthdayListCommandListener implements SlashCommandListener {

    private static final Logger log = LoggerFactory.getLogger(BirthdayListCommandListener.class);
    private static final DateTimeFormatter MM_DD = DateTimeFormatter.ofPattern("MM-dd");

    private final BirthdayService birthdayService;

    public BirthdayListCommandListener(BirthdayService birthdayService) {
        this.birthdayService = birthdayService;
    }

    @Override
    public String getCommandName() {
        return "birthday-list";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getCommandName(), "생일 전체 조회");
    }

    @Override
    public void onAction(@NotNull SlashCommandInteractionEvent event) {
        try {
            List<Birthday> birthdays = birthdayService.findAll();

            if (birthdays.isEmpty()) {
                event.reply("등록된 생일이 없습니다.").setEphemeral(true).queue();
                return;
            }

            List<String> list = new ArrayList<>();
            list.add("**생일 명단**");
            for (Birthday birthday : birthdays) {
                list.add(birthday.getId() + "|" + birthday.getUserId() + "|" + birthday.getUserName() + "|" + birthday.getBirthday().format(MM_DD));

            }

            event.reply(String.join("\n", list)).queue();
            log.info("생일 명단 조회 성공!)");

        } catch (Exception e) {
            log.error("생일 명단 조회 실패", e);
            event.reply("생일 명단 조회 실패!" + e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.MEMBER);
    }
}
