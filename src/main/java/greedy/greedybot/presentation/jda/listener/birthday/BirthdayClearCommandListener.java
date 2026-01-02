package greedy.greedybot.presentation.jda.listener.birthday;

import greedy.greedybot.application.birthday.BirthdayService;
import greedy.greedybot.presentation.jda.listener.SlashCommandListener;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BirthdayClearCommandListener implements SlashCommandListener {

    private final BirthdayService birthdayService;

    public BirthdayClearCommandListener(BirthdayService birthdayService) {
        this.birthdayService = birthdayService;
    }

    @Override
    public String getCommandName() {
        return "birthday-clear";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getCommandName(), "생일 데이터 전체 초기화");
    }

    @Override
    public void onAction(@NotNull SlashCommandInteractionEvent event) {
        birthdayService.clearAll();
        event.reply("생일 데이터가 초기화되었습니다.").queue();
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.MEMBER);
    }
}