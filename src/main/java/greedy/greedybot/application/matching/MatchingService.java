package greedy.greedybot.application.matching;

import greedy.greedybot.application.matching.dto.MatchingResult;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class MatchingService {

    private final ShuffleStrategy shuffleStrategy;

    public MatchingService(ShuffleStrategy shuffleStrategy) {
        this.shuffleStrategy = shuffleStrategy;
    }

    public MatchingResult matchStudy(final List<String> reviewees, final List<String> reviewers) {

        shuffleStrategy.shuffle(reviewers);

        if (reviewees.size() >= reviewers.size()) {
            return matchMoreReviewees(reviewees, reviewers);
        }

        return matchFewerReviewees(reviewees, reviewers);
    }

    private MatchingResult matchFewerReviewees(final List<String> reviewees, final List<String> reviewers) {
        final Map<String, List<String>> matchingResult = new HashMap<>();

        int index = 0;
        for (final String reviewee : reviewees) {
            final List<String> assignedReviewee = List.of(reviewee);
            matchingResult.put(reviewers.get(index++), assignedReviewee);
        }

        return new MatchingResult(matchingResult);
    }


    private MatchingResult matchMoreReviewees(final List<String> reviewees, final List<String> reviewers) {
        final Map<String, List<String>> matchingResult = new HashMap<>();
        final int revieweeSize = reviewees.size();
        final int reviewerSize = reviewers.size();

        final int baseCount = revieweeSize / reviewerSize;
        int remainderCount = revieweeSize % reviewerSize;

        int index = 0;

        for (final String reviewer : reviewers) {
            final int assignCount = getAssignCount(baseCount, remainderCount);
            remainderCount--;

            final List<String> assignedReviewees = reviewees.subList(index, index + assignCount);
            index += assignCount;

            matchingResult.put(reviewer, assignedReviewees);
        }
        return new MatchingResult(matchingResult);
    }

    private int getAssignCount(final int baseCount, final int remainderCount) {
        if (remainderCount > 0) {
            return baseCount + 1;
        }

        return baseCount;
    }
}
