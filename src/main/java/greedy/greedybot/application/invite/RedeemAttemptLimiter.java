package greedy.greedybot.application.invite;

import greedy.greedybot.common.exception.GreedyBotException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// 코드를 계속 바꿔가며 입력하는 것을 막는다.
// 메모리에만 두기 때문에 봇을 재시작하면 초기화되지만, 코드 자체의 경우의 수가 커서 이 정도로 충분하다.
@Component
public class RedeemAttemptLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedeemAttemptLimiter.class);

    private static final int MAX_FAILURE_COUNT = 5;
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(10);

    private final Map<String, FailureRecord> failureRecordsByUserId = new ConcurrentHashMap<>();

    public void validateNotBlocked(final String userId, final LocalDateTime now) {
        final FailureRecord record = failureRecordsByUserId.get(userId);
        if (record == null || record.isExpired(now)) {
            return;
        }
        if (record.count() >= MAX_FAILURE_COUNT) {
            log.warn("[INVITE CODE ATTEMPT BLOCKED] : {}", userId);
            throw new GreedyBotException("🚫 초대 코드를 너무 많이 틀렸습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    public void recordFailure(final String userId, final LocalDateTime now) {
        failureRecordsByUserId.merge(
            userId,
            new FailureRecord(1, now),
            (existing, added) -> existing.isExpired(now) ? added : existing.increase());
    }

    public void reset(final String userId) {
        failureRecordsByUserId.remove(userId);
    }

    private record FailureRecord(int count, LocalDateTime firstFailedAt) {

        private boolean isExpired(final LocalDateTime now) {
            return firstFailedAt.plus(FAILURE_WINDOW).isBefore(now);
        }

        private FailureRecord increase() {
            return new FailureRecord(count + 1, firstFailedAt);
        }
    }
}
