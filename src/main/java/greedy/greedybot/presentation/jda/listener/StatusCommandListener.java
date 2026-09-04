package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.presentation.jda.role.DiscordRole;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StatusCommandListener implements SlashCommandListener {

    private static final Logger log = LoggerFactory.getLogger(StatusCommandListener.class);

    private final Instant startedAt = Instant.now();

    @Override
    public String getCommandName() {
        return "status";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(this.getCommandName(), "봇의 현재 상태를 확인합니다.");
    }

    @Override
    public void onAction(@NotNull final SlashCommandInteractionEvent event) {
        final JDA jda = event.getJDA();
        final JDA.Status status = jda.getStatus();
        final long gatewayPing = jda.getGatewayPing();
        final Duration uptime = Duration.between(startedAt, Instant.now());
        log.info("[RECEIVED STATUS] : user={}, status={}, gatewayPing={}ms",
            event.getUser().getEffectiveName(), status, gatewayPing);

        event.reply("""
            ✅ **봇이 정상 동작 중입니다.**
            - 연결 상태: `%s`
            - 게이트웨이 핑: `%dms`
            - 가동 시간: `%s`
            """.formatted(status, gatewayPing, formatUptime(uptime)))
            .setEphemeral(true)
            .queue();
    }

    private String formatUptime(final Duration uptime) {
        return "%d일 %d시간 %d분 %d초".formatted(
            uptime.toDays(), uptime.toHoursPart(), uptime.toMinutesPart(), uptime.toSecondsPart());
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.values());
    }
}
