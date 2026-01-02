package greedy.greedybot.application.birthday;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.data.message-today")
public record MessageTodayData(
        int[] probabilityBox
) {
}