package greedy.greedybot.scheduledmessage;

import greedy.greedybot.scheduledmessage.domain.ScheduledMessage;
import greedy.greedybot.scheduledmessage.domain.ScheduledMessageRepository;
import greedy.greedybot.scheduledmessage.domain.ScheduledMessageService;
import greedy.greedybot.scheduledmessage.jda.listener.ScheduledCommandListener;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class ScheduledCommandListenerIntegrationTest {

    @Autowired
    private ScheduledMessageService scheduledMessageService;

    @Autowired
    private ScheduledCommandListener scheduledCommandListener;

    @Autowired
    private ScheduledMessageRepository repository;

    /**
     * ✅ 정상적인 예약 메시지 테스트
     */
    @Test
    void 예약메시지_성공() {
        // Given
        String message = "정상적인 예약 메시지입니다.";
        String timeString = LocalDateTime.now().plusMinutes(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH));
        String channelId = "testChannel";
        String userId = "testUser";

        // When
        ScheduledMessage scheduledMessage = new ScheduledMessage(message, LocalDateTime.parse(timeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH)), userId, channelId);
        scheduledMessageService.scheduleMessage(scheduledMessage);

        // Then (H2 DB에 잘 저장되었는지 확인)
        ScheduledMessage savedMessage = repository.findById(scheduledMessage.getId());
        assertThat(savedMessage).isNotNull();
        assertThat(savedMessage.getContent()).isEqualTo(message);
    }

    /**
     * ❌ 잘못된 시간 형식 입력 테스트
     */
    @Test
    void 예약메시지_실패_잘못된시간형식입니다() {
        // Given
        SlashCommandInteractionEvent mockEvent = mock(SlashCommandInteractionEvent.class);

        when(mockEvent.getOption("message")).thenReturn(mock(OptionMapping.class));
        when(mockEvent.getOption("message").getAsString()).thenReturn("잘못된 메시지");

        when(mockEvent.getOption("time")).thenReturn(mock(OptionMapping.class));
        when(mockEvent.getOption("time").getAsString()).thenReturn("03:07"); // 잘못된 형식

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scheduledCommandListener.onAction(mockEvent);
        });

        assertThat(exception.getMessage()).contains("❌ 잘못된 시간 형식입니다!");
    }

    /**
     * ❌ 과거 시간 입력 테스트
     */
    @Test
    void 예약메시지_실패_과거시간입력() {
        // Given
        SlashCommandInteractionEvent mockEvent = mock(SlashCommandInteractionEvent.class);

        when(mockEvent.getOption("message")).thenReturn(mock(OptionMapping.class));
        when(mockEvent.getOption("message").getAsString()).thenReturn("과거 메시지");

        when(mockEvent.getOption("time")).thenReturn(mock(OptionMapping.class));
        when(mockEvent.getOption("time").getAsString()).thenReturn(LocalDateTime.now().minusMinutes(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH))); // 과거 시간

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scheduledCommandListener.onAction(mockEvent);
        });

        assertThat(exception.getMessage()).contains("❌ 예약할 시간은 현재 시간 이후여야 합니다.");
    }
}
