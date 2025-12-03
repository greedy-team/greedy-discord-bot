package greedy.greedybot.presentation.jda.role;

import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.presentation.jda.role.property.DiscordRoleProperty;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DiscordRoles {

    private final Map<DiscordRole, Long> idsByDiscordRole;

    public DiscordRoles(final DiscordRoleProperty discordRoleProperty) {
        this.idsByDiscordRole = Map.of(
            DiscordRole.LEAD, discordRoleProperty.leadId(),
            DiscordRole.MEMBER, discordRoleProperty.memberId(),
            DiscordRole.COLLABORATOR, discordRoleProperty.collaboratorId(),
            DiscordRole.DEVELOPER, discordRoleProperty.developerId()
        );
    }

    public long getRoleId(final DiscordRole discordRole) {
        if (!idsByDiscordRole.containsKey(discordRole)) {
            throw new GreedyBotException("해당 역할이 존재하지 않습니다");
        }
        return idsByDiscordRole.get(discordRole);
    }
}

