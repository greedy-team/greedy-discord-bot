package greedy.greedybot.application.invite;

import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.invite.InviteCode;
import greedy.greedybot.domain.invite.InviteCodeRepository;
import java.util.ArrayList;
import java.util.List;

public class MockInviteCodeRepository implements InviteCodeRepository {

    private final List<InviteCode> inviteCodes = new ArrayList<>();

    @Override
    public void saveInviteCode(final InviteCode inviteCode) {
        inviteCodes.add(inviteCode);
    }

    @Override
    public void deleteByLabel(final String label) {
        final boolean removed = inviteCodes.removeIf(inviteCode -> inviteCode.label().equals(label));
        if (!removed) {
            throw new GreedyBotException("🚫 존재하지 않는 초대 코드입니다: " + label);
        }
    }

    @Override
    public List<InviteCode> findAll() {
        return List.copyOf(inviteCodes);
    }
}
