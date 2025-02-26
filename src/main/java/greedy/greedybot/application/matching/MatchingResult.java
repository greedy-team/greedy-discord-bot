package greedy.greedybot.application.matching;

import java.util.List;
import java.util.Map;

public class MatchingResult {
    private final Map<String, List<String>> result;

    public MatchingResult(Map<String, List<String>> result) {
        this.result = result;
    }

    public Map<String, List<String>> getResult() {
        return result;
    }
}
