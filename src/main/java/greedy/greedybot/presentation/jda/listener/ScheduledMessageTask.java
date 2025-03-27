package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.domain.message.ScheduledMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.TimerTask;

public class ScheduledMessageTask extends TimerTask {
    private final ScheduledMessage scheduledMessage;
    private final JDA jda;
    private final Runnable callback;

    public ScheduledMessageTask(ScheduledMessage scheduledMessage, JDA jda, Runnable callback) {
        this.scheduledMessage = scheduledMessage;
        this.jda = jda;
        this.callback = callback;
    }


    @Override
    public void run() {
        TextChannel channel = jda.getTextChannelById(scheduledMessage.getChannelId());

        if (channel != null) {
            channel.sendMessage(scheduledMessage.getContent()).queue();
            System.out.println("📢 예약된 메시지가 Discord 채널(" + scheduledMessage.getChannelId() + ")에 전송됨: " + scheduledMessage.getContent());
        } else {
            System.out.println("⚠ 채널을 찾을 수 없음: " + scheduledMessage.getChannelId());
        }

        callback.run();
    }
}
