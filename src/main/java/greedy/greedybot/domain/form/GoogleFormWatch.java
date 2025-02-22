package greedy.greedybot.domain.form;

import java.time.LocalDateTime;

public record GoogleFormWatch(
        String targetFormId,
        String targetFormTitle,
        int responseCount,
        LocalDateTime lastUpdatedTime
) {
    public GoogleFormWatch(String targetFormId, String targetFormTitle, int responseCount) {
        this(targetFormId, targetFormTitle, responseCount, LocalDateTime.now());
    }
}
