package greedy.greedybot.application.googleform;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled // requires google credential file
@SpringBootTest
class GoogleCredentialTest {

    @Autowired
    private GoogleCredential googleCredential;

    @Test
    void request_access_token() {
        String accessToken = googleCredential.getAccessToken();
        assertThat(accessToken).isNotEmpty();
    }

}
