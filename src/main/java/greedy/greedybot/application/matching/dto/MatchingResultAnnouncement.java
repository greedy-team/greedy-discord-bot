package greedy.greedybot.application.matching.dto;

import greedy.greedybot.application.matching.MatchingResult;

import java.util.List;
import java.util.Map;

public record MatchingResultAnnouncement(
        String announcement
) {
    public static MatchingResultAnnouncement of(final MatchingResult matchingResult) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[리뷰이]  --  [리뷰어]\n");

        for (Map.Entry<String, List<String>> entry : matchingResult.getResult().entrySet()) {
            String reviewer = entry.getKey();
            for (String reviewee : entry.getValue()) {
                stringBuilder.append(reviewee)
                        .append("    ->   ")
                        .append(reviewer)
                        .append("\n");
            }
        }
        return new MatchingResultAnnouncement(stringBuilder.toString());
    }
}
