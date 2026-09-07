package greedy.greedybot.domain.study;

import greedy.greedybot.common.exception.GreedyBotException;
import java.util.Arrays;

public enum StudyRole {
    REVIEWEE("리뷰이"),
    REVIEWER("리뷰어"),
    ;

    private final String label;

    StudyRole(final String label) {
        this.label = label;
    }

    public static StudyRole from(final String value) {
        return Arrays.stream(values())
            .filter(role -> role.name().equalsIgnoreCase(value))
            .findAny()
            .orElseThrow(() -> new GreedyBotException("🚫 존재하지 않는 역할입니다: " + value));
    }

    public String label() {
        return label;
    }
}
