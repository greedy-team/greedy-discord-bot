package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.application.googleform.GoogleFormService;
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
public class RemoveGoogleFormWatchCommandListener implements SlashCommandListener {

    private static final String FORM_ID_KEY = "form_id";
    private static final Logger log = LoggerFactory.getLogger(RemoveGoogleFormWatchCommandListener.class);

    private final GoogleFormService googleFormService;

    public RemoveGoogleFormWatchCommandListener(final GoogleFormService googleFormService) {
        this.googleFormService = googleFormService;
    }

    public String getCommandName() {
        return "form-delete";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(this.getCommandName(), "Delete google form watch")
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
        log.info("[RECEIVED DELETE FORM ID]: {}", formId);

        event.deferReply().queue();
        String removeFormWatchTitle = googleFormService.removeFormWatch(formId);
        log.info("[DELETE FORM WATCH]: {}", removeFormWatchTitle);
        event.getHook().sendMessage("""
            ✅ %s 구글폼 구독을 해제했습니다.
            """.formatted(removeFormWatchTitle)).queue();
    }
}
