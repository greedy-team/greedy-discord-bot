package greedy.greedybot.domain.matching;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BackendStudy implements Study {
    @Value("${discord.role.all.backend}")
    private String groupRoleId;

    @Override
    public String getGroupRoleId() {
        return this.groupRoleId;
    }

    @Override
    public String getStudyName() {
        return "Backend";
    }
}
