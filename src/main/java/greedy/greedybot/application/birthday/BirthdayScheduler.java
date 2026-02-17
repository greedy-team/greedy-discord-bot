package greedy.greedybot.application.birthday;

import greedy.greedybot.domain.birthday.Birthday;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;


@Component
public class BirthdayScheduler {
    private final static Logger log = LoggerFactory.getLogger(BirthdayScheduler.class);

    private final JDA jda;
    private final BirthdayService service;

    @Value("${discord.birthday_message_channel_id}")
    private String channelId;

    public BirthdayScheduler(@Lazy JDA jda, BirthdayService service) {
        this.jda = jda;
        this.service = service;

    }

    //@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Scheduled(fixedDelay = 30000)
    public void checkBirthdays() {
        List<Birthday> birthdays = service.findTodayBirthdays();
        if (birthdays.isEmpty()) {
            return;
        }

        TextChannel channel = jda.getTextChannelById(channelId);

        if (channel == null) {
            log.warn("⚠ 채널을 찾을 수 없습니다. 채널 ID: {}", channelId);
            return;
        }

        for (Birthday birthday : birthdays) {
            sendBirthdayMessage(channel, birthday);
        }

        sendTodayClosingMessage(channel);
    }

    private void sendBirthdayMessage(TextChannel channel, Birthday birthday) {
        String mention = "<@%s>".formatted(birthday.getUserId());

        String message = "%s 오늘은 **%s**님의 생일입니다!".formatted(mention, birthday.getUserName());
        channel.sendMessage(message).queue();
    }

    private void sendTodayClosingMessage(TextChannel channel) {
        String message = service.pickMessage(LocalDate.now());
        channel.sendMessage(message).queue();
        log.info("생일축하 완료");
    }
}
