package greedy.greedybot.application.googleform;

import greedy.greedybot.application.googleform.dto.EnrollFormWatchResult;
import greedy.greedybot.application.googleform.dto.client.GoogleFormInformationResponse;
import greedy.greedybot.domain.form.GoogleFormWatch;
import greedy.greedybot.domain.form.GoogleFormWatchDiscordRepository;
import org.springframework.stereotype.Service;

@Service
public class GoogleFormService {

    private final GoogleCredential googleCredential;
    private final GoogleFormApiClient googleFormApiClient;
    private final GoogleFormWatchDiscordRepository googleFormWatchDiscordRepository;

    public GoogleFormService(final GoogleCredential googleCredential,
                             final GoogleFormApiClient googleFormApiClient,
                             final GoogleFormWatchDiscordRepository googleFormWatchDiscordRepository) {
        this.googleCredential = googleCredential;
        this.googleFormApiClient = googleFormApiClient;
        this.googleFormWatchDiscordRepository = googleFormWatchDiscordRepository;
    }

    // TODO: test
    public EnrollFormWatchResult enrollFormWatch(String formId) {
        googleFormWatchDiscordRepository.findByFormId(formId).ifPresent(googleFormWatch -> {
            throw new RuntimeException("이미 등록된 구글폼 감지기입니다");
        });
        final String accessToken = googleCredential.getAccessToken();
        final GoogleFormInformationResponse formInformationResponse = googleFormApiClient.readForm(formId, accessToken);
        final int responseCount = googleFormApiClient.readFormResponseCount(formId, accessToken);
        final GoogleFormWatch formWatch = new GoogleFormWatch(formId, formInformationResponse.title(), responseCount);
        googleFormWatchDiscordRepository.saveGoogleFormWatch(formWatch);
        return new EnrollFormWatchResult(formInformationResponse.title(), responseCount);
    }
}
