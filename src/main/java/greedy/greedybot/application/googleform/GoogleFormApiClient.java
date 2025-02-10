package greedy.greedybot.application.googleform;

import greedy.greedybot.application.googleform.dto.GoogleFormInformationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GoogleFormApiClient {

    private static final String GOOGLE_FORM_API_ENDPOINT = "https://forms.googleapis.com/v1/forms/{formId}";

    private static final Logger log = LoggerFactory.getLogger(GoogleFormApiClient.class);

    private final RestClient restClient;

    public GoogleFormApiClient(final RestClient restClient) {
        this.restClient = restClient;
    }

    // ref: https://developers.google.com/forms/api/reference/rest/v1/forms/get
    public GoogleFormInformationResponse readForm(String formId, String accessToken) {
        log.info("[READ FORM INFO]: {}", formId);
        return restClient.get()
                .uri(GOOGLE_FORM_API_ENDPOINT, formId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                //.onStatus(HttpStatusCode::is4xxClientError, ) TODO: error handling
                .body(GoogleFormInformationResponse.class);
    }
}
