package greedy.greedybot.domain.invite;

import greedy.greedybot.common.exception.GreedyBotException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

// 채널 저장 포맷
// label:4기모집|role:MEMBER|hash:9f86d081...|expiresAt:2026-10-01T00:00:00|issuer:1234567890
@Component
public class InviteCodeMapper {

    private static final String FIELD_DELIMITER = "|";
    private static final String FIELD_DELIMITER_REGEX = "\\|";
    private static final String KEY_VALUE_DELIMITER = ":";
    private static final int KEY_VALUE_LIMIT = 2;

    private static final String LABEL_KEY = "label";
    private static final String ROLE_KEY = "role";
    private static final String HASH_KEY = "hash";
    private static final String EXPIRES_AT_KEY = "expiresAt";
    private static final String ISSUER_KEY = "issuer";
    private static final Set<String> REQUIRED_KEYS = Set.of(LABEL_KEY, ROLE_KEY, HASH_KEY, EXPIRES_AT_KEY, ISSUER_KEY);

    public String toTextEntity(final InviteCode inviteCode) {
        return String.join(FIELD_DELIMITER,
            toField(LABEL_KEY, inviteCode.label()),
            toField(ROLE_KEY, inviteCode.role().name()),
            toField(HASH_KEY, inviteCode.codeHash()),
            toField(EXPIRES_AT_KEY, inviteCode.expiresAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
            toField(ISSUER_KEY, inviteCode.issuerId())
        );
    }

    private String toField(final String key, final String value) {
        return key + KEY_VALUE_DELIMITER + value;
    }

    // 채널에는 사람이 남긴 일반 메세지가 섞일 수 있으므로 초대 코드 형식인지 먼저 확인한다
    public boolean isInviteCodeText(final String text) {
        return parseFields(text).keySet().containsAll(REQUIRED_KEYS);
    }

    public InviteCode toEntity(final String text) {
        final Map<String, String> fields = parseFields(text);
        return new InviteCode(
            findValueByKey(fields, LABEL_KEY),
            findValueByKey(fields, HASH_KEY),
            InvitableRole.from(findValueByKey(fields, ROLE_KEY)),
            parseExpiresAt(findValueByKey(fields, EXPIRES_AT_KEY)),
            findValueByKey(fields, ISSUER_KEY)
        );
    }

    // expiresAt 값(2026-10-01T00:00:00)에도 : 가 들어가므로 key 기준으로 한 번만 자른다
    private Map<String, String> parseFields(final String text) {
        return Arrays.stream(text.split(FIELD_DELIMITER_REGEX))
            .map(field -> field.split(KEY_VALUE_DELIMITER, KEY_VALUE_LIMIT))
            .filter(keyAndValue -> keyAndValue.length == KEY_VALUE_LIMIT)
            .collect(Collectors.toMap(
                keyAndValue -> keyAndValue[0].trim(),
                keyAndValue -> keyAndValue[1].trim(),
                (existing, replacement) -> existing
            ));
    }

    private String findValueByKey(final Map<String, String> fields, final String key) {
        final String value = fields.get(key);
        if (value == null) {
            throw new GreedyBotException("🚫 초대 코드 데이터에 " + key + " 값이 없습니다.");
        }
        return value;
    }

    private LocalDateTime parseExpiresAt(final String value) {
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new GreedyBotException("🚫 초대 코드 만료 시간 형식이 올바르지 않습니다: " + value);
        }
    }
}
