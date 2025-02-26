package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.application.googleform.GoogleFormService;
import greedy.greedybot.application.googleform.dto.EnrollFormWatchResult;
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
    private static final Logger log = LoggerFactory.getLogger(EnrollGoogleFormWatchCommandListener.class);

    private final GoogleFormService googleFormService;

    public EnrollGoogleFormWatchCommandListener(final GoogleFormService googleFormService) {
        this.googleFormService = googleFormService;
    }

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
        final OptionMapping optionalFormId = event.getOption(FORM_ID_KEY);
        if (Objects.isNull(optionalFormId)) {
            log.warn("EMPTY FORM ID");
            event.reply("Form id is required").queue();
            return;
        }
        final String formId = optionalFormId.getAsString();
        log.info("[RECEIVED ADD FORM ID]: {}", formId);

        event.deferReply().queue();
        final EnrollFormWatchResult result = googleFormService.enrollFormWatch(formId);
        log.info("[ENROLL FORM WATCH]: {}", result);
        event.getHook().sendMessage("""
                        ✅ 구글폼 응답 구독이 등록 되었어요!
                        - 제목: %s
                        - 등록된 응답 수: %d
                        """.formatted(result.formTitle(), result.responseCount()))
                .queue();
    }
}
