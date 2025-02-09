package greedy.greedybot.presentation.jda.listener;

import java.util.Objects;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EnrollGoogleFormWatchCommandListener implements SlashCommandListener {

    private static final String FORM_ID_KEY = "form_id";

    private final Logger log = LoggerFactory.getLogger(EnrollGoogleFormWatchCommandListener.class);

    public String getCommandName() {
        return "form-add";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(this.getCommandName(), "Add google form watch")
                .addOption(OptionType.STRING, FORM_ID_KEY, """
                        Google form id where it is https://docs.google.com/forms/d/{formId}
                        """);
    }

    @Override
    public void onAction(@NotNull final SlashCommandInteractionEvent event) {
        OptionMapping optionalFormId = event.getOption(FORM_ID_KEY);
        if (Objects.isNull(optionalFormId)) {
            log.warn("EMPTY FORM ID");
            event.reply("Form id is required").queue();
            return;
        }

        String formId = optionalFormId.getAsString();
        log.info("[RECEIVED ADD FORM ID]: {}", formId);

        event.reply("Delivered form id: " + formId).queue();
    }
}
