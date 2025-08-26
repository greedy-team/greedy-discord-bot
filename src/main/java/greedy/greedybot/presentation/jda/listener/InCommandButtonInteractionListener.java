package greedy.greedybot.presentation.jda.listener;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface InCommandButtonInteractionListener extends SlashCommandListener {
    void onButtonInteraction(ButtonInteractionEvent event);
    boolean isSupportingButtonId(String buttonId);
}
