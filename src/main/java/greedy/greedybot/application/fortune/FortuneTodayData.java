package greedy.greedybot.application.fortune;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discord.data.fortune-today")
public record FortuneTodayData(
        int[] probabilityBox
) {
}
