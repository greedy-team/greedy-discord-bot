package greedy.greedybot.presentation.jda.role.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discord.messagereceivingchannel")
public record ScheduledMessageChannelProperty(
        long greedyNoticeId,
        long greedyBackendStudy,
        long greedyFrontendStudy,
        long leadLeadConversation,
        long tfDiscordTestGroud
) {
}
