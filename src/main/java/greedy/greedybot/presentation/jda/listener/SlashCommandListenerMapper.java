package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.common.exception.GreedyBotException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SlashCommandListenerMapper extends ListenerAdapter {

    private final Logger log = LoggerFactory.getLogger(SlashCommandListenerMapper.class);

    private final Map<String, SlashCommandListener> slashCommandListenersByCommandName;

    public SlashCommandListenerMapper(final Set<SlashCommandListener> slashCommandListeners) {
        this.slashCommandListenersByCommandName = slashCommandListeners.stream()
                .collect(Collectors.toMap(SlashCommandListener::getCommandName, it -> it));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
        final String commandName = event.getName();

        final SlashCommandListener slashCommand = slashCommandListenersByCommandName.get(commandName);
        if (slashCommand == null) {
            log.error("NOT FOUND COMMAND: {}", commandName);
            return;
        }

        log.info("[RECEIVED DISCORD SLASH COMMAND] : {}", commandName);
        try {
            slashCommand.onAction(event);
        } catch (GreedyBotException e) {
            log.warn("[WARN]: {}", e.getMessage());
            event.getHook().sendMessage(e.getMessage()).queue();
        } catch (Exception e) {
            log.error("[ERROR OCCURRED]: {}, {}", commandName, e.getStackTrace());
            event.getHook().sendMessage(e.getMessage()).queue();
        }
    }
}
