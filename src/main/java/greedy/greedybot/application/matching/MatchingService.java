package greedy.greedybot.application.matching;

import greedy.greedybot.application.matching.dto.MatchingResult;
import greedy.greedybot.common.exception.GreedyBotException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;


@Service
public class MatchingService {

    private final ShuffleStrategy shuffleStrategy;

    public MatchingService(ShuffleStrategy shuffleStrategy) {
        this.shuffleStrategy = shuffleStrategy;
    }

    public MatchingResult matchStudy(final List<String> reviewees, final List<String> reviewers) {
        shuffleStrategy.shuffle(reviewers);
        shuffleStrategy.shuffle(reviewees);
        final List<String> alignedReviewer = alignReviewerByRevieweeSize(reviewees.size(), reviewers);
        return match(reviewees, alignedReviewer);
    }

    private List<String> alignReviewerByRevieweeSize(final int revieweeSize, final List<String> shuffledReviewers) {
        if (revieweeSize > shuffledReviewers.size()) {
            return alignWhenReviewersAreLess(revieweeSize, shuffledReviewers);
        }

        if (revieweeSize < shuffledReviewers.size()) {
            return alignWhenReviewersAreMore(revieweeSize, shuffledReviewers);
        }

        return shuffledReviewers;
    }

    // 리뷰어가 부족할 때, 리뷰어를 리뷰이 수 만큼 추가 선택
    // ex) 리뷰이 6명, 리뷰어 4명 -> 리뷰어 4명 + 리뷰어 2명 추가 선택 |
    //     리뷰어: [a, b, c, d] -> [a, b, c, d, a, b]
    private List<String> alignWhenReviewersAreLess(final int revieweeSize, final List<String> shuffledReviewers) {
        final int additionalReviewerCount = revieweeSize - shuffledReviewers.size();
        final List<String> result = new ArrayList<>(shuffledReviewers);
        for (int i = 0; i < additionalReviewerCount; i++) {
            result.add(shuffledReviewers.get(i));
        }
        return result;
    }

    // 리뷰어가 더 많을 때, 리뷰이 사람 수 만큼 리뷰어 선택
    // ex) 리뷰이 4명, 리뷰어 6명 -> 리뷰어 4명만 선택
    //     리뷰어: [a, b, c, d, e, f] -> [a, b, c, d]
    private List<String> alignWhenReviewersAreMore(final int revieweeSize, final List<String> shuffledReviewers) {
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
