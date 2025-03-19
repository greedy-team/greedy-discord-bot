package greedy.greedybot.presentation.jda.role.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discord.role")
public record DiscordRoleProperty(
        long leadId,
        long memberId,
        long collaboratorId
) {
}
