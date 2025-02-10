package greedy.greedybot.application.googleform.dto;

import java.util.List;

public record GoogleFormResponsesData(
        List<Response> responses
) {

    public record Response(
            String responseId
    ) {
    }
}
