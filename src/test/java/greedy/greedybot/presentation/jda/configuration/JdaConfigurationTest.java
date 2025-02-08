package greedy.greedybot.presentation.jda.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JdaConfigurationTest {

    @Autowired
    private JDA jda;

    @Autowired
    private Guild greedyGuild;

    @Test
    void setup_jda() {
        assertThat(jda).isNotNull();
        assertThat(greedyGuild).isNotNull();
    }
}
