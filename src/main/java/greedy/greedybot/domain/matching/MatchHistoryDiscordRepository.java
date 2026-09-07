package greedy.greedybot.domain.matching;

import java.util.Optional;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
public class MatchHistoryDiscordRepository implements MatchHistoryRepository {

    private static final int MAX_HISTORY_SIZE = 100;

    private final TextChannel matchHistoryChannel;
    private final MatchHistoryMapper matchHistoryMapper;

    public MatchHistoryDiscordRepository(@Lazy final TextChannel matchHistoryChannel,
                                         final MatchHistoryMapper matchHistoryMapper) {
        this.matchHistoryChannel = matchHistoryChannel;
        this.matchHistoryMapper = matchHistoryMapper;
    }

    // 리뷰이 그룹 당 한 건만 유지 하므로 기존 기록이 있으면 덮어 쓴다
    @Override
    public void saveMatchHistory(final MatchHistory matchHistory) {
        final String matchHistoryText = matchHistoryMapper.toTextEntity(matchHistory);
        findMessageByRevieweeGroupId(matchHistory.revieweeGroupId())
            .ifPresentOrElse(
                message -> message.editMessage(matchHistoryText).queue(),
                () -> matchHistoryChannel.sendMessage(matchHistoryText).queue()
            );
    }

    @Override
    public Optional<MatchHistory> findByRevieweeGroupId(final String revieweeGroupId) {
        return findMessageByRevieweeGroupId(revieweeGroupId)
            .map(message -> matchHistoryMapper.toEntity(message.getContentDisplay()));
    }

    private Optional<Message> findMessageByRevieweeGroupId(final String revieweeGroupId) {
        return matchHistoryChannel.getHistory().retrievePast(MAX_HISTORY_SIZE).complete()
            .stream()
            .filter(message -> matchHistoryMapper.isMatchHistoryText(message.getContentDisplay()))
            .filter(message -> matchHistoryMapper.toEntity(message.getContentDisplay())
                .revieweeGroupId().equals(revieweeGroupId))
            .findAny();
    }
}
