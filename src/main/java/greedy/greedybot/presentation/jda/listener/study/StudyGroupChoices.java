package greedy.greedybot.presentation.jda.listener.study;

import greedy.greedybot.domain.study.StudyGroup;
import java.util.List;
import net.dv8tion.jda.api.interactions.commands.Command;

public final class StudyGroupChoices {

    // 디스코드 자동 완성 제약: 선택지 최대 25개, 이름 최대 100자
    private static final int MAX_CHOICE_SIZE = 25;
    private static final int MAX_CHOICE_NAME_LENGTH = 100;
    private static final String TRUNCATED_SUFFIX = "…";

    private StudyGroupChoices() {
    }

    public static List<Command.Choice> from(final List<StudyGroup> studyGroups, final String focusedValue) {
        return studyGroups.stream()
            .filter(studyGroup -> matches(studyGroup, focusedValue))
            .limit(MAX_CHOICE_SIZE)
            .map(studyGroup -> new Command.Choice(truncate(studyGroup.describe()), studyGroup.id()))
            .toList();
    }

    private static boolean matches(final StudyGroup studyGroup, final String focusedValue) {
        final String keyword = focusedValue.toLowerCase();
        return studyGroup.id().toLowerCase().contains(keyword)
            || studyGroup.describe().toLowerCase().contains(keyword);
    }

    private static String truncate(final String name) {
        if (name.length() <= MAX_CHOICE_NAME_LENGTH) {
            return name;
        }
        return name.substring(0, MAX_CHOICE_NAME_LENGTH - TRUNCATED_SUFFIX.length()) + TRUNCATED_SUFFIX;
    }
}
