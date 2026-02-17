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

import java.util.Set;

@Component
public class BirthdayDeleteCommandListener implements SlashCommandListener {
    private final static Logger log = LoggerFactory.getLogger(BirthdayDeleteCommandListener.class);
    private final BirthdayService birthdayService;

    public BirthdayDeleteCommandListener(BirthdayService birthdayService) {
        this.birthdayService = birthdayService;
    }

    @Override
    public String getCommandName() {
        return "birthday-delete";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getCommandName(), "생일 삭제")
                .addOption(OptionType.STRING, "userid", "삭제할 디스코드 아이디", true);
    }

    @Override
    public void onAction(@NotNull SlashCommandInteractionEvent event) {
        try {
            OptionMapping idOpt = event.getOption("userid");

            if (idOpt == null) {
                event.reply("아이디를 입력해주세요").setEphemeral(true).queue();
                return;
            }

            String userId = idOpt.getAsString().trim();
            Birthday birthday = birthdayService.findByUserId(userId)
                    .orElseThrow(() -> new GreedyBotException("등록된 생일 정보가 없습니다"));
            String userName = birthday.getUserName();

            birthdayService.delete(userId);

            log.info("생일 삭제 완료 Id: {}", userId);
            event.reply("생일 삭제 완료! [" + userId + "|" + userName + "]").queue();
        } catch (GreedyBotException e) {
            log.error("생일 삭제 실패", e);
            event.reply("생일 삭제 실패! " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.DEVELOPER);
    }
}
