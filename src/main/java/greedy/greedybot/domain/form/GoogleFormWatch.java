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

    public GoogleFormWatch updateResponseCount(int responseCount) {
        return new GoogleFormWatch(targetFormId, targetFormTitle, responseCount, LocalDateTime.now());
    }

    public boolean hasNewResponse(final int responseCount) {
        return this.responseCount() < responseCount;
    }
}
