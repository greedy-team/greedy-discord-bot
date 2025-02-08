package greedy.greedybot.presentation.jda.configuration;

import greedy.greedybot.presentation.jda.listener.StatusCommandListener;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SlashCommandLoader {

    private static final Logger log = LoggerFactory.getLogger(SlashCommandLoader.class);

    private final Guild greedyGuild;
    private final StatusCommandListener statusCommandListener;

    public SlashCommandLoader(final Guild greedyGuild, final StatusCommandListener statusCommandListener) {
        this.greedyGuild = greedyGuild;
        this.statusCommandListener = statusCommandListener;
    }

    @PostConstruct
    void loadCommands() {
        log.info("Load command: {}", statusCommandListener.getCommandName());

        greedyGuild.updateCommands().addCommands(
                Commands.slash(statusCommandListener.getCommandName(), "Check the status of the bot")
                        .addOption(OptionType.STRING, "ping", "Ping the bot")
                        .addOption(OptionType.STRING, "ok", "Check the status of the bot")
                        .addOption(OptionType.STRING, "hello", "Hello the bot")
        ).queue();
    }
}
