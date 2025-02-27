package greedy.greedybot.scheduledMessage.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class ScheduledMessage {

    private final String id;
    private final String content;
    private final LocalDateTime scheduledTime;
    private final String userId;
    private final String channelId;

    public ScheduledMessage(String content, LocalDateTime scheduledTime, String userId, String channelId) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.scheduledTime = scheduledTime;
        this.userId = userId;
        this.channelId = channelId;
    }

    public String getId() {
        return id;
    }
    public String getContent() {
        return content;
    }
    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }
    public String getUserId() {
        return userId;
    }
    public String getChannelId() {
        return channelId;
    }
}
