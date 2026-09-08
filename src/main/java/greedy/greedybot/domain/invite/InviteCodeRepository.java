package greedy.greedybot.domain.invite;

import java.util.List;

public interface InviteCodeRepository {

    void saveInviteCode(InviteCode inviteCode);

    void deleteByLabel(String label);

    List<InviteCode> findAll();
}
