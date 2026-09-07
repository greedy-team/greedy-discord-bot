package greedy.greedybot.application.matching.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MatchingResult {

    private static final String REPEATED_MARK = "  ⚠️ 지난 미션과 동일";

    private final Map<String, List<String>> revieweesByReviewer;
    // 지난 매칭에서도 같은 리뷰어를 받은 리뷰이
    private final Set<String> repeatedReviewees;

    public MatchingResult(final Map<String, List<String>> revieweesByReviewer) {
        this(revieweesByReviewer, Set.of());
    }

    public MatchingResult(final Map<String, List<String>> revieweesByReviewer,
                          final Set<String> repeatedReviewees) {
        this.revieweesByReviewer = revieweesByReviewer;
        this.repeatedReviewees = Set.copyOf(repeatedReviewees);
    }

    public MatchingResult withRepeatedReviewees(final Set<String> repeatedReviewees) {
        return new MatchingResult(revieweesByReviewer, repeatedReviewees);
    }

    public Map<String, String> toReviewerByReviewee() {
        final Map<String, String> reviewerByReviewee = new LinkedHashMap<>();
        revieweesByReviewer.forEach((reviewer, reviewees) ->
            reviewees.forEach(reviewee -> reviewerByReviewee.put(reviewee, reviewer)));
        return reviewerByReviewee;
    }

    public int repeatedCount() {
        return repeatedReviewees.size();
    }

    public String toDiscordAnnouncement() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[리뷰이]  --  [리뷰어]\n");

        for (final String reviewer : revieweesByReviewer.keySet()) {
            final List<String> reviewees = revieweesByReviewer.get(reviewer);
            reviewees.forEach(reviewee -> stringBuilder.append(reviewee)
                    .append("    ->   ")
                    .append(reviewer)
                    .append(markIfRepeated(reviewee))
                    .append("\n"));
        }

        appendRepeatedWarning(stringBuilder);
        return stringBuilder.toString();
    }

    private String markIfRepeated(final String reviewee) {
        if (repeatedReviewees.contains(reviewee)) {
            return REPEATED_MARK;
        }
        return "";
    }

    private void appendRepeatedWarning(final StringBuilder stringBuilder) {
        if (repeatedReviewees.isEmpty()) {
            return;
        }
        stringBuilder.append("\n⚠️ ")
            .append(repeatedReviewees.size())
            .append("건은 지난 매칭과 겹칩니다. 재시도해도 겹칠 수 있어요.\n");
    }
}
