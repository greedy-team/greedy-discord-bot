package greedy.greedybot.domain.study;

import greedy.greedybot.common.exception.GreedyBotException;
import java.util.Arrays;

public enum StudyType {
    FRONTEND("FE"),
    BACKEND("BE"),
    ;

    private final String label;

    StudyType(final String label) {
        this.label = label;
    }

    public static StudyType from(final String value) {
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(value))
            .findAny()
            .orElseThrow(() -> new GreedyBotException("🚫 존재하지 않는 스터디 타입입니다: " + value));
    }

    public String label() {
        return label;
    }
}
