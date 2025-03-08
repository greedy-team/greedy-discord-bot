package greedy.greedybot.application.googleform;

import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class GoogleCredential {

    @Value("${google.form.scope}")
    private String FORMS_SCOPE;

    @Value("${google.form.credentials_path}")
    private String SERVICE_ACCOUNT_PATH;

    public String getAccessToken() {
        try {
            InputStream credentialsStream = new ClassPathResource(SERVICE_ACCOUNT_PATH).getInputStream();
            final GoogleCredentials credentials = GoogleCredentials
                    .fromStream(credentialsStream)
                    .createScoped(Collections.singleton(FORMS_SCOPE));

            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new RuntimeException("Not Found Google credential file", e);
        }
    }
}
