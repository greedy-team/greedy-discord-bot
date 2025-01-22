package greedy.greedybot.presentation.jda;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JDAConfig {

    @Value("${discord.token}")
    private String token;

    @PostConstruct
    void setUpJDA() {
        JDABuilder.createLight(token)
                .build();
    }
}
