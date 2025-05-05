package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import greedy.greedybot.presentation.jda.role.DiscordRoles;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SlashCommandListenerMapper extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SlashCommandListenerMapper.class);

    private final Map<String, SlashCommandListener> slashCommandListenersByCommandName;
    private final DiscordRoles discordRoles;

    public SlashCommandListenerMapper(final Set<SlashCommandListener> slashCommandListeners,
                                      final DiscordRoles discordRoles) {
        this.slashCommandListenersByCommandName = slashCommandListeners.stream()
                .collect(Collectors.toMap(SlashCommandListener::getCommandName, it -> it));
        this.discordRoles = discordRoles;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
        final String commandName = event.getName();
        final SlashCommandListener slashCommand = slashCommandListenersByCommandName.get(commandName);
        log.info("[RECEIVED DISCORD SLASH COMMAND] : {}", commandName);
        if (!hasRole(event, slashCommand.allowedRoles())) {
            log.warn("[COMMAND PERMISSION DENIED]: {}", event.getMember().getNickname());
            event.reply("이 명령어를 사용할 권한이 없습니다.").setEphemeral(true).queue();
        }

        try {
            slashCommand.onAction(event);
        } catch (GreedyBotException e) {
            log.warn("[WARN]: {}", e.getMessage());
            event.reply("❌" + e.getMessage()).setEphemeral(true).queue();
        } catch (Exception e) {
            log.error("[ERROR OCCURRED]: {}, {}", commandName, e.getStackTrace());
            event.getHook().sendMessage(e.getMessage()).queue();
        }
    }

    private boolean hasRole(final @NotNull SlashCommandInteractionEvent event,
                            final Set<DiscordRole> allowedRoles) {
        final Set<Long> allowedRoleIds = allowedRoles.stream()
                .map(discordRoles::getRoleId)
                .collect(Collectors.toSet());
        return event.getMember().getRoles().stream()
                .anyMatch(role -> allowedRoleIds.contains(role.getIdLong()));
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
