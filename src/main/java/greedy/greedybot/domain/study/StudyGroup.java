package greedy.greedybot.domain.study;

import greedy.greedybot.common.exception.GreedyBotException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

// 스터디 한 기수의 리뷰이 그룹 또는 리뷰어 그룹
// ex) id: be-4, role: REVIEWEE, type: BACKEND, generation: 4, members: [이태규, 김민욱]
//     id: be-4-java, role: REVIEWER, type: BACKEND, generation: 4, members: [정다빈, 조상준]
public record StudyGroup(
    String id,
    StudyRole role,
    StudyType type,
    int generation,
    List<String> members
) {

    // 저장 포맷의 구분자와 충돌 하는 문자는 id, 멤버 이름에 사용할 수 없다
    private static final Pattern FORBIDDEN_CHARACTERS = Pattern.compile("[|:,\\r\\n]");
    private static final int MIN_GENERATION = 1;

    public StudyGroup {
        validateId(id);
        validateGeneration(generation);
        validateMembers(members);
        members = List.copyOf(members);
    }

    private static void validateId(final String id) {
        if (Objects.isNull(id) || id.isBlank()) {
            throw new GreedyBotException("🚫 스터디 그룹 id가 비어 있습니다.");
        }
        if (FORBIDDEN_CHARACTERS.matcher(id).find()) {
            throw new GreedyBotException("🚫 스터디 그룹 id에는 `|` `:` `,` 를 사용할 수 없습니다: " + id);
        }
    }

    private static void validateGeneration(final int generation) {
        if (generation < MIN_GENERATION) {
            throw new GreedyBotException("🚫 기수는 " + MIN_GENERATION + " 이상의 숫자여야 합니다: " + generation);
        }
    }

    private static void validateMembers(final List<String> members) {
        if (Objects.isNull(members) || members.isEmpty()) {
            throw new GreedyBotException("🚫 스터디 그룹의 멤버가 비어 있습니다.");
        }
        members.stream()
            .filter(member -> FORBIDDEN_CHARACTERS.matcher(member).find())
            .findAny()
            .ifPresent(member -> {
                throw new GreedyBotException("🚫 멤버 이름에는 `|` `:` `,` 를 사용할 수 없습니다: " + member);
            });
    }

    // 리뷰이 그룹과 리뷰어 그룹이 같은 스터디에 속하는지 판별
    public boolean isSameStudyWith(final StudyGroup other) {
        return type == other.type && generation == other.generation;
    }

    public boolean hasRole(final StudyRole role) {
        return this.role == role;
    }

    public String describe() {
        return "[%s %d기] %s · %s".formatted(type.label(), generation, id, String.join(", ", members));
    }

    public String studyName() {
        return "%s %d기".formatted(type.label(), generation);
    }
}
