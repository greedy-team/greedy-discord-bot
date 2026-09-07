package greedy.greedybot.domain.matching;

import greedy.greedybot.common.exception.GreedyBotException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

// 채널 저장 포맷
// revieweeGroupId:be-4|mission:장바구니|pairs:이태규>정다빈,김민욱>조상준
@Component
public class MatchHistoryMapper {

    private static final String FIELD_DELIMITER = "|";
    private static final String FIELD_DELIMITER_REGEX = "\\|";
    private static final String KEY_VALUE_DELIMITER = ":";
    private static final String PAIR_DELIMITER = ",";
    private static final String REVIEWEE_REVIEWER_DELIMITER = ">";
    private static final int KEY_VALUE_LIMIT = 2;
    private static final int PAIR_SIZE = 2;

    private static final String REVIEWEE_GROUP_ID_KEY = "revieweeGroupId";
    private static final String MISSION_KEY = "mission";
    private static final String PAIRS_KEY = "pairs";
    private static final Set<String> REQUIRED_KEYS = Set.of(REVIEWEE_GROUP_ID_KEY, MISSION_KEY, PAIRS_KEY);

    public String toTextEntity(final MatchHistory matchHistory) {
        final String pairs = matchHistory.reviewerByReviewee().entrySet().stream()
            .map(pair -> pair.getKey() + REVIEWEE_REVIEWER_DELIMITER + pair.getValue())
            .collect(Collectors.joining(PAIR_DELIMITER));

        return String.join(FIELD_DELIMITER,
            REVIEWEE_GROUP_ID_KEY + KEY_VALUE_DELIMITER + matchHistory.revieweeGroupId(),
            MISSION_KEY + KEY_VALUE_DELIMITER + matchHistory.mission(),
            PAIRS_KEY + KEY_VALUE_DELIMITER + pairs
        );
    }

    // 채널에는 사람이 남긴 일반 메세지가 섞일 수 있으므로 매칭 기록 형식인지 먼저 확인한다
    public boolean isMatchHistoryText(final String text) {
        return parseFields(text).keySet().containsAll(REQUIRED_KEYS);
    }

    public MatchHistory toEntity(final String text) {
        final Map<String, String> fields = parseFields(text);
        return new MatchHistory(
            findValueByKey(fields, REVIEWEE_GROUP_ID_KEY),
            findValueByKey(fields, MISSION_KEY),
            parsePairs(findValueByKey(fields, PAIRS_KEY))
        );
    }

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
            throw new GreedyBotException("🚫 매칭 기록에 " + key + " 값이 없습니다.");
        }
        return value;
    }

    private Map<String, String> parsePairs(final String rawPairs) {
        final Map<String, String> reviewerByReviewee = new LinkedHashMap<>();
        Arrays.stream(rawPairs.split(PAIR_DELIMITER))
            .map(String::trim)
            .filter(pair -> !pair.isBlank())
            .map(pair -> pair.split(REVIEWEE_REVIEWER_DELIMITER, PAIR_SIZE))
            .filter(pair -> pair.length == PAIR_SIZE)
            .forEach(pair -> reviewerByReviewee.put(pair[0].trim(), pair[1].trim()));
        return reviewerByReviewee;
    }
}
