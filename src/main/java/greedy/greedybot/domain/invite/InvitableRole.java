package greedy.greedybot.domain.invite;

import greedy.greedybot.common.exception.GreedyBotException;
import java.util.Arrays;

// 초대 코드로 부여할 수 있는 역할만 정의한다.
// LEAD, DEVELOPER 처럼 권한이 큰 역할은 코드가 유출되더라도 부여될 수 없도록 아예 넣지 않는다.
public enum InvitableRole {
    MEMBER("멤버"),
    COLLABORATOR("콜라보레이터"),
    ;

    private final String label;

    InvitableRole(final String label) {
        this.label = label;
    }

    public static InvitableRole from(final String value) {
        return Arrays.stream(values())
            .filter(role -> role.name().equalsIgnoreCase(value))
            .findAny()
            .orElseThrow(() -> new GreedyBotException("🚫 초대 코드로 부여할 수 없는 역할입니다: " + value));
    }

    public String label() {
        return label;
    }
}
