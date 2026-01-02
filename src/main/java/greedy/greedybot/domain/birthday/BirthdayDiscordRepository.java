package greedy.greedybot.domain.birthday;

import greedy.greedybot.common.exception.GreedyBotException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BirthdayDiscordRepository implements BirthdayRepository {
    private static final DateTimeFormatter MM_DD = DateTimeFormatter.ofPattern("MM-dd");

    private final TextChannel birhtdayChannel;

    public BirthdayDiscordRepository(@Lazy TextChannel birthdayChannel) {
        this.birhtdayChannel = birthdayChannel;
    }

    @Override
    public void save(Birthday birthday) {
        birhtdayChannel.sendMessage(
                birthday.getId() + "|"
                        + birthday.getUserId() + "|"
                        + birthday.getUserName() + "|"
                        + birthday.getBirthday().format(MM_DD)
        ).queue();
    }

    @Override
    public void delete(String userId) {
        birhtdayChannel.getHistory().retrievePast(50).complete()
                .stream()
                .filter(message -> {
                    String[] parts = message.getContentRaw().split("\\|");
                    return parts.length == 4 && parts[1].equals(userId);
                })
                .findFirst()
                .orElseThrow(() -> new GreedyBotException("생일이 저장되어 있지 않습니다."))
                .delete()
                .queue();
    }


    @Override
    public Optional<Birthday> findByUserId(String userId) {
        return birhtdayChannel.getHistory().retrievePast(50).complete()
                .stream()
                .filter(message -> {
                    String[] parts = message.getContentRaw().split("\\|");
                    return parts.length == 4 && parts[1].equals(userId);
                })
                .map(message -> {
                    String[] parts = message.getContentRaw().split("\\|");
                    return new Birthday(
                            parts[0],
                            parts[1],
                            parts[2],
                            MonthDay.parse(parts[3].trim(), MM_DD)
                    );
                })
                .findFirst();
    }

    @Override
    public List<Birthday> findAll() {
        List<Message> messages = birhtdayChannel.getHistory().retrievePast(50).complete();
        List<Birthday> result = new ArrayList<>();
        for (Message message : messages) {
            String[] parts = message.getContentRaw().split("\\|");
            if (parts.length != 4) continue;

            String date = parts[3].trim();
            Birthday birthday = new Birthday(
                    parts[0],
                    parts[1],
                    parts[2],
                    MonthDay.parse(date, MM_DD)
            );

            result.add(birthday);

        }
        return result;
    }

    @Override
    public List<Birthday> findByMonthDay(int month, int day) {
        List<Message> messages = birhtdayChannel.getHistory().retrievePast(50).complete();
        List<Birthday> result = new ArrayList<>();
        for (Message message : messages) {
            String[] parts = message.getContentRaw().split("\\|");
            if (parts.length != 4) continue;

            String date = parts[3].trim();
            Birthday birthday = new Birthday(
                    parts[0],
                    parts[1],
                    parts[2],
                    MonthDay.parse(date, MM_DD)
            );

            if (birthday.isSameMonthDay(month, day)) {
                result.add(birthday);
            }
        }
        return result;
    }

    @Override
    public void clearAll() {
        List<Message> messages = birhtdayChannel.getHistory().retrievePast(100).complete();
        for (Message message : messages) {
            String[] parts = message.getContentRaw().split("\\|");
            if (parts.length != 4) continue;

            String date = parts[3].trim();
            if (shouldDelete(date)) {
                message.delete().queue();
            }
        }
    }
    private boolean shouldDelete(String date) {
        if (date.startsWith("--")) return true;
        return isMmDd(date);
    }

    private boolean isMmDd(String date) {
        String d = date.trim();
        String[] parts = d.split("-");
        if (parts.length != 2) return false;

        if (parts[0].length() != 2 || parts[1].length() != 2) return false;

        int mm, dd;
        try {
            mm = Integer.parseInt(parts[0]);
            dd = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        if (mm < 1 || mm > 12) return false;
        if (dd < 1 || dd > 31) return false;

        return true;
    }
}