package greedy.greedybot.application.googleform.dto.client;

public record GoogleFormInformationResponse(
        Info info
) {
    public String title() {
        return info.title();
    }

    record Info(
            String title,
            String description,
            String documentTitle
    ) {
    }
}
