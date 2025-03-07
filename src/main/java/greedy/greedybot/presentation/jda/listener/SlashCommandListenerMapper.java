package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.common.exception.GreedyBotException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SlashCommandListenerMapper extends ListenerAdapter {

    private final Logger log = LoggerFactory.getLogger(SlashCommandListenerMapper.class);

    private final Map<String, SlashCommandListener> slashCommandListenersByCommandName;

    @Value("${discord.command.permission_id}")
    private String COMMAND_PERMISSION_ID;

    public SlashCommandListenerMapper(final Set<SlashCommandListener> slashCommandListeners) {
        this.slashCommandListenersByCommandName = slashCommandListeners.stream()
                .collect(Collectors.toMap(SlashCommandListener::getCommandName, it -> it));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
        final boolean hasRole = event.getMember().getRoles().stream()
                .anyMatch(role -> role.getId().equals(COMMAND_PERMISSION_ID));
        if (!hasRole) {
            log.warn("[COMMAND PERMISSION DENIED]: {}", event.getMember().getNickname());
            event.reply("이 명령어를 사용할 권한이 없습니다.").setEphemeral(true).queue();
        }

        run(event);
    }

    private void run(@NotNull final SlashCommandInteractionEvent event) {
        final String commandName = event.getName();
        final SlashCommandListener slashCommand = slashCommandListenersByCommandName.get(commandName);
        log.info("[RECEIVED DISCORD SLASH COMMAND] : {}", commandName);

        try {
            slashCommand.onAction(event);
        } catch (GreedyBotException e) {
            log.warn("[WARN]: {}", e.getMessage());
            event.getHook().sendMessage("❌" + e.getMessage()).queue();
        } catch (Exception e) {
            log.error("[ERROR OCCURRED]: {}, {}", commandName, e.getStackTrace());
            event.getHook().sendMessage(e.getMessage()).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull final CommandAutoCompleteInteractionEvent event) {
        final String commandName = event.getName();

        final SlashCommandListener slashCommand = slashCommandListenersByCommandName.get(commandName);
        log.info("[RECEIVED DISCORD AUTOCOMPLETE COMMAND] : {}", commandName);
        if (slashCommand instanceof AutoCompleteInteractionListener autoCompleteInteractionListener) {
            autoCompleteInteractionListener.onCommandAutoCompleteInteraction(event);
        }
    }
}
