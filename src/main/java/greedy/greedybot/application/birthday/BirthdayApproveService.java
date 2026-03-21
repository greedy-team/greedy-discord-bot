package greedy.greedybot.application.birthday;

import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.birthday.Birthday;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

@Service
public class BirthdayApproveService {
    private final static Logger log = LoggerFactory.getLogger(BirthdayApproveService.class);
    private static final DateTimeFormatter MM_DD = DateTimeFormatter.ofPattern("MM-dd");

    private final BirthdayService birthdayService;

    public BirthdayApproveService(BirthdayService birthdayService) {
        this.birthdayService = birthdayService;
    }

    public void approve(Message message) {
        ParsedRequest req = parsedRequest(message);

        MonthDay md;
        try {
            md = MonthDay.parse(req.mmdd(), MM_DD);
        } catch (DateTimeException e) {
            throw new GreedyBotException("메시지의 생일 형식이 잘못되었습니다.");
        }
        Birthday birthday = new Birthday(req.userId(), req.userName, md);

        birthdayService.register(birthday);
        log.info("생일 승인 완료 userId={}, birthday={}", req.userId(), req.mmdd());
    }

    public void reject(Message message) {
        ParsedRequest req = parsedRequest(message);
        log.info("생일 요청 거절 userId={}, birthday={}", req.userId(), req.mmdd());
    }

    private ParsedRequest parsedRequest(Message message) {
        if (message.getEmbeds().isEmpty()) {
            throw new GreedyBotException("Embed 메시지가 없습니다.");
        }

        MessageEmbed embed = message.getEmbeds().get(0);

        String userId = getFiledVale(embed, "Id");
        String userName = getFiledVale(embed, "이름");
        String mmdd = getFiledVale(embed, "생일");

        if (userId == null || userId.isBlank()) {
            throw new GreedyBotException("Embed 메시지에서 userId를 찾을 수 없습니다.");
        }
        if (userName == null || userName.isBlank()) {
            throw new GreedyBotException("Embed 메시지에서 이름을 찾을 수 없습니다.");
        }
        if (mmdd == null || mmdd.isBlank()) {
            throw new GreedyBotException("Embed 메시지에서 생일(mmdd)을 찾을 수 없습니다.");
        }

        return new ParsedRequest(userId.trim(), userName.trim(), mmdd.trim());
    }

    private String getFiledVale(MessageEmbed embed, String fieldName) {
        return embed.getFields().stream()
                .filter(f -> fieldName.equals(f.getName()))
                .findFirst()
                .map(MessageEmbed.Field::getValue)
                .orElse(null);
    }

    private record ParsedRequest(String userId, String userName, String mmdd) {

    }
}
