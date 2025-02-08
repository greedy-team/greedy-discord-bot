package greedy.greedybot.presentation.jda.configuration;

import greedy.greedybot.presentation.jda.listener.SlashCommandListener;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SlashCommandLoader {

    private static final Logger log = LoggerFactory.getLogger(SlashCommandLoader.class);

    private final Guild greedyGuild;
    private final Set<SlashCommandListener> slashCommandListeners;

    public SlashCommandLoader(final Guild greedyGuild, final Set<SlashCommandListener> slashCommandListeners) {
        this.greedyGuild = greedyGuild;
        this.slashCommandListeners = slashCommandListeners;
    }

    @PostConstruct
    void loadCommands() {
        List<String> commandNames = slashCommandListeners.stream()
                .map(SlashCommandListener::getCommandName)
                .toList();
        log.info("[LOAD COMMANDS]: {}", commandNames);

        List<SlashCommandData> commandData = slashCommandListeners.stream()
                .map(SlashCommandListener::getCommandData)
                .toList();
        greedyGuild.updateCommands().addCommands(commandData).queue();
        log.info("[COMMANDS LOADED SUCCESSFULLY]: {}", commandNames);
    }
}
