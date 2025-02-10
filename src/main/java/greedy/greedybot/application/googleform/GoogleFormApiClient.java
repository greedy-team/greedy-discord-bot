package greedy.greedybot.application.googleform;

import greedy.greedybot.application.googleform.dto.client.GoogleFormInformationResponse;
import greedy.greedybot.application.googleform.dto.client.GoogleFormResponsesData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GoogleFormApiClient {

    private static final String GET_FORM_ENDPOINT = "https://forms.googleapis.com/v1/forms/{formId}";
    private static final String LIST_RESPONSE_ENDPOINT = "https://forms.googleapis.com/v1/forms/{formId}/responses";

    private static final Logger log = LoggerFactory.getLogger(GoogleFormApiClient.class);

    private final RestClient restClient;

    public GoogleFormApiClient(final RestClient restClient) {
        this.restClient = restClient;
    }

    // ref: https://developers.google.com/forms/api/reference/rest/v1/forms/get
    public GoogleFormInformationResponse readForm(String formId, String accessToken) {
        return restClient.get()
                .uri(GET_FORM_ENDPOINT, formId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                //.onStatus(HttpStatusCode::is4xxClientError, ) TODO: error handling
                .body(GoogleFormInformationResponse.class);
    }

    // ref: https://developers.google.com/forms/api/reference/rest/v1/forms.responses/list
    public int readFormResponseCount(String formId, String accessToken) {
        return restClient.get()
                .uri(LIST_RESPONSE_ENDPOINT, formId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                //.onStatus(HttpStatusCode::is4xxClientError, ) TODO: error handling
                .body(GoogleFormResponsesData.class)
                .responses()
                .size();
    }
}
