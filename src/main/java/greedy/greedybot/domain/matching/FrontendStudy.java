package greedy.greedybot.domain.matching;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FrontendStudy implements Study {
    @Value("${discord.role.all.frontend}")
    private String groupRoleId;

    @Override
    public String getGroupRoleId() {
        return this.groupRoleId;
    }

    @Override
    public String getStudyName() {
        return "Frontend";
    }
}
