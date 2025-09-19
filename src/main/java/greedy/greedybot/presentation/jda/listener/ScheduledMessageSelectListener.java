package greedy.greedybot.presentation.jda.listener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class ScheduledMessageSelectListener extends ListenerAdapter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm",
        Locale.ENGLISH);

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        String[] parts = event.getComponentId().split(":", 2);
        String componentId = parts[0];
        String mentionRaw = (parts.length > 1) ? parts[1] : null;

        if (!componentId.equals("scheduled-channel-select")) {
            return;
        }

        TextInput messageInput = TextInput.create("message", "메시지", TextInputStyle.PARAGRAPH)
            .setRequired(true)
            .build();

        final String defaultTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        TextInput timeInput = TextInput.create("time", "예약 시간", TextInputStyle.SHORT)
            .setValue(defaultTime)
            .setRequired(true)
            .build();

        final String channelId = event.getValues().get(0);

        String modalId = "scheduled-message-modal:" + channelId;
        if (mentionRaw != null && !mentionRaw.isEmpty()) {
            modalId += ":" + mentionRaw;
        }

        Modal modal = Modal.create(modalId, "예약 메시지 등록")
            .addActionRow(messageInput)
            .addActionRow(timeInput)
            .build();

        event.replyModal(modal).queue();
    }
}
