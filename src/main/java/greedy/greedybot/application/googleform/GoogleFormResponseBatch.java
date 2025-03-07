package greedy.greedybot.application.googleform;

import greedy.greedybot.domain.form.GoogleFormWatch;
import greedy.greedybot.domain.form.GoogleFormWatchRepository;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class GoogleFormResponseBatch {

    private static final Logger log = LoggerFactory.getLogger(GoogleFormResponseBatch.class);

    private final GoogleFormWatchRepository googleFormWatchDiscordRepository;
    private final GoogleFormApiClient googleFormApiClient;
    private final GoogleCredential googleCredential;
    private final JDA jda;

    @Value("${discord.google_form_alert_channel_id}")
    private String googleFormAlertChannelId;

    public GoogleFormResponseBatch(final GoogleFormWatchRepository googleFormWatchDiscordRepository,
                                   final GoogleFormApiClient googleFormApiClient,
                                   final GoogleCredential googleCredential,
                                   final JDA jda) {
        this.googleFormWatchDiscordRepository = googleFormWatchDiscordRepository;
        this.googleFormApiClient = googleFormApiClient;
        this.googleCredential = googleCredential;
        this.jda = jda;
    }

    @Scheduled(initialDelay = 60 * 1000, fixedDelay = 1000 * 60 * 5) // 5분마다 실행
    public void fetchGoogleFormWatchResponses() {
        final String accessToken = googleCredential.getAccessToken();
        final List<GoogleFormWatch> googleFormWatches = googleFormWatchDiscordRepository.findAll();
        for (final GoogleFormWatch googleFormWatch : googleFormWatches) {
            log.info("[FETCH GOOGLE FORM RESPONSES] formId: {}", googleFormWatch.targetFormId());
            final String formId = googleFormWatch.targetFormId();
            final int responseCount = googleFormApiClient.readFormResponseCount(formId, accessToken);

            if (googleFormWatch.hasNewResponse(responseCount)) {
                updateGoogleFormWatchResponseCount(googleFormWatch, responseCount);
            }
        }
    }

    private void updateGoogleFormWatchResponseCount(final GoogleFormWatch googleFormWatch, final int responseCount) {
        final int previousResponseCount = googleFormWatch.responseCount();
        final GoogleFormWatch updateGoogleFormWatch = googleFormWatch.updateResponseCount(responseCount);
        googleFormWatchDiscordRepository.updateGoogleFormWatch(updateGoogleFormWatch);
        final int newResponseCount = responseCount - previousResponseCount;
        jda.getTextChannelById(googleFormAlertChannelId).sendMessage("""
                📩 %s에 %d개의 새로운 응답이 도착했어요! 총 응답 수: %d
                """.formatted(updateGoogleFormWatch.targetFormTitle(), newResponseCount, responseCount)
        ).queue();
    }
}
