package greedy.greedybot.domain.birthday;

import greedy.greedybot.common.exception.GreedyBotException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BirthdayStaticRepository {
    private static final List<String> DUMMY_MESSEAGES = List.of(
            """
            ### 랜덤1 축하
            """,
            """
            ### 랜덤2 축하
            """,
            """
            ### 랜덤3 축하
            """,
            """
            ### 랜덤4 축하
            """
    );
    public int getSize() {
        return DUMMY_MESSEAGES.size();
    }

    public String getMessageByIndex(final int todayMessageIndex) {
        if (todayMessageIndex < 0 || todayMessageIndex >= DUMMY_MESSEAGES.size()) {
            throw new GreedyBotException("오늘의 메시지를 찾을 수 없습니다.");
        }
        return DUMMY_MESSEAGES.get(todayMessageIndex);
    }
}