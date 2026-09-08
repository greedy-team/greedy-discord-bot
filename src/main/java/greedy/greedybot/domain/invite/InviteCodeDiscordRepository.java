package greedy.greedybot.domain.invite;

import greedy.greedybot.common.exception.GreedyBotException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
public class InviteCodeDiscordRepository implements InviteCodeRepository {

    private static final int MAX_HISTORY_SIZE = 100;

    private final TextChannel inviteCodeChannel;
    private final InviteCodeMapper inviteCodeMapper;

    public InviteCodeDiscordRepository(@Lazy final TextChannel inviteCodeChannel,
                                       final InviteCodeMapper inviteCodeMapper) {
        this.inviteCodeChannel = inviteCodeChannel;
        this.inviteCodeMapper = inviteCodeMapper;
    }

    @Override
    public void saveInviteCode(final InviteCode inviteCode) {
        final String inviteCodeText = inviteCodeMapper.toTextEntity(inviteCode);
        inviteCodeChannel.sendMessage(inviteCodeText).complete();
    }

    @Override
    public void deleteByLabel(final String label) {
        findMessageByLabel(label)
            .orElseThrow(() -> new GreedyBotException("🚫 존재하지 않는 초대 코드입니다: " + label))
            .delete()
            .complete();
    }

    @Override
    public List<InviteCode> findAll() {
        return inviteCodeMessages()
            .map(message -> inviteCodeMapper.toEntity(message.getContentDisplay()))
            .toList();
    }

    private Optional<Message> findMessageByLabel(final String label) {
        return inviteCodeMessages()
            .filter(message -> inviteCodeMapper.toEntity(message.getContentDisplay()).label().equals(label))
            .findAny();
    }

    private Stream<Message> inviteCodeMessages() {
        return inviteCodeChannel.getHistory().retrievePast(MAX_HISTORY_SIZE).complete()
            .stream()
            .filter(message -> inviteCodeMapper.isInviteCodeText(message.getContentDisplay()));
    }
}
