package greedy.greedybot.application.googleform.dto.client;

public record GoogleFormInformationResponse(
        Info info
) {
    record Info(
            String title,
            String description,
            String documentTitle
    ) {
    }

    public String title() {
        return info.title();
    }
}
