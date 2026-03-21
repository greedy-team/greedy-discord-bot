package greedy.greedybot.application.birthday;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class BirthdayRequestService {
    private final static Logger log = LoggerFactory.getLogger(BirthdayRequestService.class);

    private final JDA jda;

    @Value("${discord.birthday_request_channel_id}")
    private String channelId;

    public BirthdayRequestService(@Lazy JDA jda) {
        this.jda = jda;
    }

    public void submitRequest(String userId, String mmdd, String reason, String displayName) {
        TextChannel channel = jda.getTextChannelById(channelId);

        if (channel == null) {
            log.warn("⚠ 채널을 찾을 수 없습니다. 채널 ID: {}", channelId);
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("생일 등록 요청")
                .addField("Id", userId, false)
                .addField("이름", displayName, true)
                .addField("생일", mmdd, true)
                .addField("사유", reason, false);

        ActionRow row = ActionRow.of(
                Button.success("birthday:approve", "✅ 승인"),
                Button.danger("birthday:reject", "❌ 거절"));

        channel.sendMessageEmbeds(embedBuilder.build()).setComponents(row).queue();
        log.info("생일 등록 요청 전송 userId={}, , userName{}, birthday={}, reason={}", userId, displayName, mmdd, reason);
    }
}
