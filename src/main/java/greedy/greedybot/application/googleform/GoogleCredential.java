package greedy.greedybot.application.googleform;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleCredential {

    @Value("${google.form.scope}")
    private String FORMS_SCOPE;

    @Value("${google.form.credentials_path}")
    private String SERVICE_ACCOUNT_PATH;

    public String getAccessToken() {
        try {
            final GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new FileInputStream(SERVICE_ACCOUNT_PATH))
                    .createScoped(Collections.singleton(FORMS_SCOPE));

            credentials.refreshIfExpired();
            AccessToken token = credentials.getAccessToken();
            return token.getTokenValue();
        } catch (IOException e) {
            throw new RuntimeException("Not Found Google credential file", e);
        }
    }
}
