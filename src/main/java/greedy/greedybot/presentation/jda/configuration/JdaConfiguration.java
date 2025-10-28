package greedy.greedybot.presentation.jda.configuration;

import greedy.greedybot.presentation.jda.listener.ScheduledMessageModalLauncher;
import greedy.greedybot.presentation.jda.listener.ScheduledMessageSubmitListener;
import greedy.greedybot.presentation.jda.listener.SlashCommandListenerMapper;
import java.util.EnumSet;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JdaConfiguration {

    private final SlashCommandListenerMapper slashCommandListenerMapper;
    private final ScheduledMessageModalLauncher scheduledMessageModalLauncher;
    private final ScheduledMessageSubmitListener scheduledMessageSubmitListener;

    @Value("${discord.token}")
    private String token;
    @Value("${discord.guild_id}")
    private String guildId;
    @Value("${discord.google_form_watch_channel_id}")
    private String googleFormChannelId;
    @Value("${discord.scheduled_message_channel_id}")
    private String scheduledMessageChannelId;

    public JdaConfiguration(SlashCommandListenerMapper slashCommandListenerMapper,
        ScheduledMessageModalLauncher scheduledMessageModalLauncher,
        ScheduledMessageSubmitListener scheduledMessageSubmitListener) {
        this.slashCommandListenerMapper = slashCommandListenerMapper;
        this.scheduledMessageModalLauncher = scheduledMessageModalLauncher;
        this.scheduledMessageSubmitListener = scheduledMessageSubmitListener;
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
                .addEventListeners( // 수동 입력
                    slashCommandListenerMapper,
                    scheduledMessageSubmitListener
                )
                .enableIntents(intents)
                .build()
                .awaitReady(); // https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/JDABuilder.html#build()
    }

    @Bean
    Guild greedyGuild(final JDA jda) {
        return jda.getGuildById(guildId);
    }

    @Bean
    TextChannel googleFormWatchChannel(final JDA jda) {
        return jda.getTextChannelById(googleFormChannelId);
    }

    @Bean
    TextChannel scheduledMessageChannel(final JDA jda) {
        return jda.getTextChannelById(scheduledMessageChannelId);
    }
}
