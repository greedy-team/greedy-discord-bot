package greedy.greedybot.application.matching.dto;

import java.util.List;
import java.util.Map;

public class MatchingResult {
    private final Map<String, List<String>> revieweesByReviewer;

    public MatchingResult(Map<String, List<String>> revieweesByReviewer) {
        this.revieweesByReviewer = revieweesByReviewer;
    }

    public String toDiscordAnnouncement() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[리뷰이]  --  [리뷰어]\n");

        for (final String reviewer : revieweesByReviewer.keySet()) {
            final List<String> reviewees = revieweesByReviewer.get(reviewer);
            reviewees.forEach(reviewee -> stringBuilder.append(reviewee)
                    .append("    ->   ")
                    .append(reviewer)
                    .append("\n"));
        }

        return stringBuilder.toString();
    }
}
