package greedy.greedybot.domain.matching;

import java.util.Optional;

public interface MatchHistoryRepository {

    void saveMatchHistory(MatchHistory matchHistory);

    Optional<MatchHistory> findByRevieweeGroupId(String revieweeGroupId);
}
