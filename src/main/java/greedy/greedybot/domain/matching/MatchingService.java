package greedy.greedybot.domain.matching;

import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class MatchingService {

    public String matchStudy(final List<String> reviewees, final List<String> reviewers) {
        MatchingResult matchingResult;

        Collections.shuffle(reviewees);
        Collections.shuffle(reviewers);

        if (reviewees.size() >= reviewers.size()) {
            matchingResult = matchMoreReviewees(reviewees, reviewers);
            return matchingResult.toDiscordNotification();
        }

        matchingResult = matchFewerReviewees(reviewees, reviewers);
        return matchingResult.toDiscordNotification();
    }

    private MatchingResult matchFewerReviewees(final List<String> reviewees, final List<String> reviewers) {
        final Map<String, List<String>> matchingResult = new HashMap<>();

        int index = 0;
        for (String reviewee : reviewees) {
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

        for (String reviewer : reviewers) {
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
