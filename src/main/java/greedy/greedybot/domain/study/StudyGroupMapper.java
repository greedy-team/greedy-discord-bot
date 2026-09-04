package greedy.greedybot.domain.study;

import greedy.greedybot.common.exception.GreedyBotException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

// 채널 저장 포맷
// id:be-4-java|role:REVIEWER|type:BACKEND|generation:4|members:정다빈,조상준,이진
@Component
public class StudyGroupMapper {

    private static final String FIELD_DELIMITER = "|";
    private static final String FIELD_DELIMITER_REGEX = "\\|";
    private static final String KEY_VALUE_DELIMITER = ":";
    private static final String MEMBER_DELIMITER = ",";
    private static final int KEY_VALUE_LIMIT = 2;

    private static final String ID_KEY = "id";
    private static final String ROLE_KEY = "role";
    private static final String TYPE_KEY = "type";
    private static final String GENERATION_KEY = "generation";
    private static final String MEMBERS_KEY = "members";
    private static final Set<String> REQUIRED_KEYS = Set.of(ID_KEY, ROLE_KEY, TYPE_KEY, GENERATION_KEY, MEMBERS_KEY);

    public String toTextEntity(final StudyGroup studyGroup) {
        return String.join(FIELD_DELIMITER,
            toField(ID_KEY, studyGroup.id()),
            toField(ROLE_KEY, studyGroup.role().name()),
            toField(TYPE_KEY, studyGroup.type().name()),
            toField(GENERATION_KEY, String.valueOf(studyGroup.generation())),
            toField(MEMBERS_KEY, String.join(MEMBER_DELIMITER, studyGroup.members()))
        );
    }

    private String toField(final String key, final String value) {
        return key + KEY_VALUE_DELIMITER + value;
    }

    // 채널에는 사람이 남긴 일반 메세지가 섞일 수 있으므로 스터디 그룹 형식인지 먼저 확인한다
    public boolean isStudyGroupText(final String text) {
        return parseFields(text).keySet().containsAll(REQUIRED_KEYS);
    }

    public StudyGroup toEntity(final String text) {
        final Map<String, String> fields = parseFields(text);
        return new StudyGroup(
            findValueByKey(fields, ID_KEY),
            StudyRole.from(findValueByKey(fields, ROLE_KEY)),
            StudyType.from(findValueByKey(fields, TYPE_KEY)),
            parseGeneration(findValueByKey(fields, GENERATION_KEY)),
            parseMembers(findValueByKey(fields, MEMBERS_KEY))
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
            throw new GreedyBotException("🚫 스터디 그룹 데이터에 " + key + " 값이 없습니다.");
        }
        return value;
    }

    private int parseGeneration(final String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new GreedyBotException("🚫 기수는 숫자여야 합니다: " + value);
        }
    }

    public List<String> parseMembers(final String rawMembers) {
        return Arrays.stream(rawMembers.split(MEMBER_DELIMITER))
            .map(String::trim)
            .filter(member -> !member.isBlank())
            .toList();
    }
}
