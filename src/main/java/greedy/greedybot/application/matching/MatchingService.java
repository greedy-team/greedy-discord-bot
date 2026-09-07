package greedy.greedybot.application.matching;

import greedy.greedybot.application.matching.dto.MatchingResult;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.matching.MatchHistory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class MatchingService {

    private static final int MAX_MATCH_ATTEMPTS = 30;

    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);

    private final ShuffleStrategy shuffleStrategy;

    public MatchingService(ShuffleStrategy shuffleStrategy) {
        this.shuffleStrategy = shuffleStrategy;
    }

    public MatchingResult matchStudy(final List<String> reviewees, final List<String> reviewers) {
        return matchStudy(reviewees, reviewers, MatchHistory.empty("none"));
    }

    // 지난 매칭에서와 같은 리뷰어를 받은 리뷰이가 없을 때까지 다시 섞는다.
    // 겹치지 않는 조합이 아예 없을 수도 있으므로 시도 횟수를 제한하고,
    // 그 중 가장 적게 겹치는 결과를 사용한다.
    public MatchingResult matchStudy(final List<String> reviewees,
                                     final List<String> reviewers,
                                     final MatchHistory previousMatch) {
        validateEnoughReviewers(reviewees, reviewers);

        MatchingResult bestResult = null;
        Set<String> bestRepeatedReviewees = null;

        for (int attempt = 0; attempt < MAX_MATCH_ATTEMPTS; attempt++) {
            final MatchingResult result = matchOnce(reviewees, reviewers);
            final Set<String> repeatedReviewees = findRepeatedReviewees(result, previousMatch);
            if (repeatedReviewees.isEmpty()) {
                return result;
            }
            if (bestRepeatedReviewees == null || repeatedReviewees.size() < bestRepeatedReviewees.size()) {
                bestResult = result;
                bestRepeatedReviewees = repeatedReviewees;
            }
        }

        log.info("[MATCHING REPEATED PAIRS] : {}", bestRepeatedReviewees);
        return bestResult.withRepeatedReviewees(bestRepeatedReviewees);
    }

    // 한 리뷰어가 여러 리뷰이를 맡지 않도록 리뷰어가 리뷰이보다 적으면 매칭하지 않는다
    private void validateEnoughReviewers(final List<String> reviewees, final List<String> reviewers) {
        if (reviewers.size() < reviewees.size()) {
            log.warn("[NOT ENOUGH REVIEWERS] : 리뷰이 {}명, 리뷰어 {}명", reviewees.size(), reviewers.size());
            throw new GreedyBotException(
                "🚫 리뷰어가 리뷰이보다 적습니다. (리뷰이 %d명, 리뷰어 %d명)".formatted(reviewees.size(), reviewers.size()));
        }
    }

    private Set<String> findRepeatedReviewees(final MatchingResult result, final MatchHistory previousMatch) {
        if (previousMatch.isEmpty()) {
            return Set.of();
        }
        return result.toReviewerByReviewee().entrySet().stream()
            .filter(pair -> previousMatch.isSameReviewerAsBefore(pair.getKey(), pair.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    // 전달 받은 목록을 그대로 섞으면 호출측 목록이 변경 되므로 복사본을 섞는다
    private MatchingResult matchOnce(final List<String> reviewees, final List<String> reviewers) {
        final List<String> shuffledReviewees = new ArrayList<>(reviewees);
        final List<String> shuffledReviewers = new ArrayList<>(reviewers);
        shuffleStrategy.shuffle(shuffledReviewers);
        shuffleStrategy.shuffle(shuffledReviewees);
        final List<String> alignedReviewer =
            alignReviewerByRevieweeSize(shuffledReviewees.size(), shuffledReviewers);
        return match(shuffledReviewees, alignedReviewer);
    }

    // 리뷰어는 항상 리뷰이 이상이므로, 리뷰이 사람 수 만큼 리뷰어를 선택한다
    // ex) 리뷰이 4명, 리뷰어 6명 -> 리뷰어 4명만 선택
    //     리뷰어: [a, b, c, d, e, f] -> [a, b, c, d]
    private List<String> alignReviewerByRevieweeSize(final int revieweeSize, final List<String> shuffledReviewers) {
        return shuffledReviewers.subList(0, revieweeSize);
    }

    private MatchingResult match(final List<String> reviewees, final List<String> reviewers) {
        if (!validateNumberOfCases(reviewees, reviewers)) {
            throw new GreedyBotException("매칭 경우의 수가 존재 하지 않습니다");
        }

        final Map<String, List<String>> matchedReviewers = reviewers.stream()
            .collect(Collectors.toMap(
                key -> key,
                value -> new ArrayList<>(),
                (existing, replacement) -> existing
            ));

        final List<String> removableReviewees = new ArrayList<>(reviewees);
        for (final String reviewer : reviewers) {
            int cursor = 0;
            while (!removableReviewees.isEmpty()) {
                final String reviewee = removableReviewees.get(cursor);
                if (reviewer.equals(reviewee)) {
                    cursor++;
                    continue;
                }
                matchedReviewers.get(reviewer).add(reviewee);
                removableReviewees.remove(reviewee);
                break;
            }
        }
        return new MatchingResult(matchedReviewers);
    }

    // 이름이 같은 리뷰어-리뷰이 매칭이 불가능한 로직을 준수하는 경우의 수 검증
    // ex) 리뷰어: [a, a, c] 리뷰이: [a, a, d] -> 매칭 불가능
    private boolean validateNumberOfCases(final List<String> reviewees, final List<String> reviewers) {
        final Map<String, Long> countByReviewerName = reviewers.stream()
            .collect(Collectors.groupingBy(it -> it, Collectors.counting()));

        for (final String reviewee : reviewees) {
            long nonSameNameCount = reviewers.size() - countByReviewerName.getOrDefault(reviewee, 0L);
            if (nonSameNameCount <= 0) {
                return false;
            }
        }

        return true;
    }
}
