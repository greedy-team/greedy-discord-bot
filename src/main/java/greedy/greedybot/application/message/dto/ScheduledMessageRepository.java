package greedy.greedybot.application.message.dto;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ScheduledMessageRepository {
    private final Map<String, ScheduledMessage> scheduledMessages = new ConcurrentHashMap<>();

    public void save(ScheduledMessage message){
        scheduledMessages.put(message.getId(), message);
    }

    public void delete(String id){
        scheduledMessages.remove(id);
    }

    public ScheduledMessage findById (String id){
        return scheduledMessages.get(id);
    }

}
