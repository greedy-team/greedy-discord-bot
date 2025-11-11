package greedy.greedybot.presentation.jda.role;

import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.presentation.jda.role.property.ScheduledMessageChannelProperty;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ScheduledMessageChannels {

    private final Map<ScheduledMessageChannel, Long> channels;

    public ScheduledMessageChannels(final ScheduledMessageChannelProperty scheduledMessageChannelProperty) {
        this.channels = Map.of(
            ScheduledMessageChannel.NOTICE, scheduledMessageChannelProperty.greedyNoticeId(),
            ScheduledMessageChannel.BACKEND, scheduledMessageChannelProperty.greedyBackendStudy(),
            ScheduledMessageChannel.FRONT, scheduledMessageChannelProperty.greedyFrontendStudy(),
            ScheduledMessageChannel.TEST, scheduledMessageChannelProperty.tfDiscordTestGroud()
        );
    }

    public long getChannelId(final ScheduledMessageChannel scheduledMessageChannel) {
        if (!channels.containsKey(scheduledMessageChannel)) {
            throw new GreedyBotException("해당 채널이 존재하지 않습니다");
        }
        return channels.get(scheduledMessageChannel);
    }
}
