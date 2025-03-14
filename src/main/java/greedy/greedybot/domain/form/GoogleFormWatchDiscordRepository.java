package greedy.greedybot.domain.form;

import greedy.greedybot.common.exception.GreedyBotException;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
public class GoogleFormWatchDiscordRepository implements GoogleFormWatchRepository {

    private final TextChannel googleFormWatchChannel;
    private final GoogleFormWatchMapper googleFormWatchMapper;

    public GoogleFormWatchDiscordRepository(@Lazy final TextChannel googleFormWatchChannel,
                                            final GoogleFormWatchMapper googleFormWatchMapper) {
        this.googleFormWatchChannel = googleFormWatchChannel;
        this.googleFormWatchMapper = googleFormWatchMapper;
    }

    @Override
    public void saveGoogleFormWatch(final GoogleFormWatch googleFormWatch) {
        final String googleFormWatchText = googleFormWatchMapper.toTextEntity(googleFormWatch);
        googleFormWatchChannel.sendMessage(googleFormWatchText).queue();
    }

    @Override
    public void deleteByFormId(final String formId) {
        googleFormWatchChannel.getHistory().retrievePast(100).complete()
                .stream()
                .filter(message -> message.getContentDisplay().contains(formId))
                .findAny()
                .orElseThrow(() -> new GreedyBotException("존재 하지 않는 구글폼 감지기입니다"))
                .delete()
                .queue();
    }

    @Override
    public Optional<GoogleFormWatch> findByFormId(final String formId) {
        return googleFormWatchChannel.getHistory().retrievePast(100).complete()
                .stream()
                .filter(message -> message.getContentDisplay().contains(formId))
                .findAny()
                .map(message -> googleFormWatchMapper.toEntity(message.getContentDisplay()));
    }

    @Override
    public List<GoogleFormWatch> findAll() {
        return googleFormWatchChannel.getHistory().retrievePast(100).complete()
                .stream()
                .map(message -> googleFormWatchMapper.toEntity(message.getContentDisplay()))
                .toList();
    }

    @Override
    public void updateGoogleFormWatch(final GoogleFormWatch googleFormWatch) {
        googleFormWatchChannel.getHistory().retrievePast(100).complete()
                .stream()
                .filter(message -> message.getContentDisplay().contains(googleFormWatch.targetFormId()))
                .findAny()
                .orElseThrow(() -> new GreedyBotException("존재 하지 않는 구글폼 감지기입니다"))
                .editMessage(googleFormWatchMapper.toTextEntity(googleFormWatch))
                .queue();
    }
}
