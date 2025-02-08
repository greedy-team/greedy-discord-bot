package greedy.greedybot.presentation.jda.listener;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StatusCommandListener extends ListenerAdapter {

    private final Logger log = LoggerFactory.getLogger(StatusCommandListener.class);

    @Override
    public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
        final String command = event.getName();
        log.info("[RECEIVED DISCORD SLASH COMMAND] : {}", command);
        event.reply("Ok!").queue();
        String ping = event.getOption("ping").getAsString();
        String ok = event.getOption("ok").getAsString();
        String hello = event.getOption("hello").getAsString();

        log.info("ping: {}", ping);
        log.info("ok: {}", ok);
        log.info("hello: {}", hello);
    }

    public String getCommandName() {
        return "status";
    }
}
