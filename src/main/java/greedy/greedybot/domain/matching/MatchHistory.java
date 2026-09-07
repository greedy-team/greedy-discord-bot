package greedy.greedybot.domain.matching;

import greedy.greedybot.common.exception.GreedyBotException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

// 직전 확정된 매칭 기록. 리뷰이 그룹 당 한 건만 유지한다.
// 리뷰어 그룹이 아닌 리뷰이 그룹을 기준으로 하는 이유는 로테이션(fe-4-1, fe-4-2)이 바뀌어도
// "지난 미션에 누구에게 리뷰 받았는지" 를 이어서 확인해야 하기 때문이다.
public record MatchHistory(
    String revieweeGroupId,
    String mission,
    Map<String, String> reviewerByReviewee
) {

    public MatchHistory {
        if (Objects.isNull(revieweeGroupId) || revieweeGroupId.isBlank()) {
            throw new GreedyBotException("🚫 매칭 기록의 리뷰이 그룹 id가 비어 있습니다.");
        }
        // Map.copyOf 는 순회 순서가 JVM 실행마다 달라져 저장 문자열이 매번 바뀌므로 입력 순서를 유지한다
        reviewerByReviewee = Collections.unmodifiableMap(new LinkedHashMap<>(reviewerByReviewee));
    }

    public static MatchHistory empty(final String revieweeGroupId) {
        return new MatchHistory(revieweeGroupId, "", Map.of());
    }

    public boolean isSameReviewerAsBefore(final String reviewee, final String reviewer) {
        return reviewer.equals(reviewerByReviewee.get(reviewee));
    }

    public boolean isEmpty() {
        return reviewerByReviewee.isEmpty();
    }
}
