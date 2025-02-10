package greedy.greedybot.application.googleform;

import greedy.greedybot.application.googleform.dto.EnrollFormWatchResult;
import greedy.greedybot.application.googleform.dto.client.GoogleFormInformationResponse;
import org.springframework.stereotype.Service;

@Service
public class GoogleFormService {

    private final GoogleCredential googleCredential;
    private final GoogleFormApiClient googleFormApiClient;

    public GoogleFormService(final GoogleCredential googleCredential, final GoogleFormApiClient googleFormApiClient) {
        this.googleCredential = googleCredential;
        this.googleFormApiClient = googleFormApiClient;
    }

    // TODO: test
    public EnrollFormWatchResult enrollFormWatch(String formId) {
        String accessToken = googleCredential.getAccessToken();
        GoogleFormInformationResponse googleFormInformationResponse = googleFormApiClient.readForm(formId, accessToken);
        int responseCount = googleFormApiClient.readFormResponseCount(formId, accessToken);
        return new EnrollFormWatchResult(googleFormInformationResponse.title(), responseCount);
    }
}
