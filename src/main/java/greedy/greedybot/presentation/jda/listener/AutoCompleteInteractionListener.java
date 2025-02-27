package greedy.greedybot.presentation.jda.listener;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

public interface AutoCompleteInteractionListener extends SlashCommandListener {
    void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event);
}
