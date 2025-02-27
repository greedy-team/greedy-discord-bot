package greedy.greedybot.application.matching.dto;

import java.util.List;
import java.util.Map;

public class MatchingResult {
    private final Map<String, List<String>> result;

    public MatchingResult(Map<String, List<String>> result) {
        this.result = result;
    }

    public String toDiscordAnnouncement() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[리뷰이]  --  [리뷰어]\n");

        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            String reviewer = entry.getKey();
            for (String reviewee : entry.getValue()) {
                stringBuilder.append(reviewee)
                        .append("    ->   ")
                        .append(reviewer)
                        .append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
