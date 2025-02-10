package greedy.greedybot.application.googleform.dto.client;

import java.util.List;

public record GoogleFormResponsesData(
        List<Response> responses
) {

    record Response(
            String responseId
    ) {
    }
}
