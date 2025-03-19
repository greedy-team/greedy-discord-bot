package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.presentation.jda.role.DiscordRole;
import java.util.List;
import java.util.Set;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public interface SlashCommandListener {

    // command name for discord slash command
    String getCommandName();

    // command data, including options
    SlashCommandData getCommandData();

    // action to be performed when the command is invoked
    void onAction(@NotNull final SlashCommandInteractionEvent event);

    Set<DiscordRole> allowedRoles();
}
