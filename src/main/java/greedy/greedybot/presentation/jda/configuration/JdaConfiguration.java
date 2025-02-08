package greedy.greedybot.presentation.jda.configuration;

import java.util.EnumSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JdaConfiguration {

    @Value("${discord.token}")
    private String token;

    @Bean
    JDA jda() {
        final EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.SCHEDULED_EVENTS
        );

        return JDABuilder.createLight(token)
                .setActivity(Activity.listening("메세지 입력 대기"))
                .setStatus(OnlineStatus.ONLINE)
                .enableIntents(intents)
                .build();
    }
}
