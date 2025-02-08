package greedy.greedybot.presentation.jda.configuration;

import greedy.greedybot.presentation.jda.listener.StatusCommandListener;
import java.util.EnumSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JdaConfiguration {

    @Value("${discord.token}")
    private String token;

    @Value("${discord.guild_id}")
    private String guildId;

    private final StatusCommandListener statusCommandListener;

    public JdaConfiguration(final StatusCommandListener statusCommandListener) {
        this.statusCommandListener = statusCommandListener;
    }

    @Bean
    JDA jda() throws InterruptedException {
        // ref https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/requests/GatewayIntent.html
        final EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.SCHEDULED_EVENTS
        );

        return JDABuilder.createLight(token)
                .setActivity(Activity.listening("메세지 입력"))
                .setStatus(OnlineStatus.ONLINE)
                .addEventListeners(statusCommandListener)
                .enableIntents(intents)
                .build()
                .awaitReady(); // https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/JDABuilder.html#build()
    }

    @Bean
    Guild greedyGuild(JDA jda) {
        return jda.getGuildById(guildId);
    }
}
