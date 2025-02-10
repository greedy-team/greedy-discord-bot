package greedy.greedybot.application.googleform.dto;

public record GoogleFormInformationResponse(
        Info info
) {
    public record Info(
            String title,
            String description,
            String documentTitle
    ) {
    }
}
