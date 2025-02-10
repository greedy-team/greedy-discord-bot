package greedy.greedybot.domain.form;

import java.util.Optional;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
public class GoogleFormWatchDiscordRepository {

    private final TextChannel googleFormWatchChannel;
    private final GoogleFormWatchMapper googleFormWatchMapper;

    public GoogleFormWatchDiscordRepository(@Lazy final TextChannel googleFormWatchChannel,
                                            final GoogleFormWatchMapper googleFormWatchMapper) {
        this.googleFormWatchChannel = googleFormWatchChannel;
        this.googleFormWatchMapper = googleFormWatchMapper;
    }

    public void saveGoogleFormWatch(final GoogleFormWatch googleFormWatch) {
        final String googleFormWatchText = googleFormWatchMapper.toTextEntity(googleFormWatch);
        googleFormWatchChannel.sendMessage(googleFormWatchText).queue();
    }

    public void deleteByFormId(final String formId) {
        googleFormWatchChannel.getHistory().retrievePast(100).complete()
                .stream()
                .filter(message -> message.getContentDisplay().contains(formId))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Not Found FormId"));
    }

    public Optional<GoogleFormWatch> findByFormId(final String formId) {
        return googleFormWatchChannel.getHistory().retrievePast(100).complete()
                .stream()
                .filter(message -> message.getContentDisplay().contains(formId))
                .findAny()
                .map(message -> googleFormWatchMapper.toEntity(message.getContentDisplay()));
    }
}
