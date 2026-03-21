package greedy.greedybot.presentation.jda.listener.birthday;

import greedy.greedybot.application.birthday.BirthdayService;
import greedy.greedybot.common.exception.GreedyBotException;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MemberRemoveListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(MemberRemoveListener.class);

    private final BirthdayService birthdayService;

    public MemberRemoveListener(BirthdayService birthdayService) {
        this.birthdayService = birthdayService;
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        String userId = event.getUser().getId();
        try {
            birthdayService.delete(userId);
        } catch (GreedyBotException ignored) {
        }

        log.info("멤버 서버 탈퇴로 인한 생일 데이터 삭제 userId={}, guildId={}", userId, event.getGuild().getId());
    }
}
