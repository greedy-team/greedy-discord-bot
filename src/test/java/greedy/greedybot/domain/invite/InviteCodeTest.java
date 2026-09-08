package greedy.greedybot.domain.invite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import greedy.greedybot.common.exception.GreedyBotException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InviteCodeTest {

    private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2026, 10, 1, 0, 0);

    private InviteCode inviteCode(final String label) {
        return new InviteCode(label, "hash", InvitableRole.MEMBER, EXPIRES_AT, "1234");
    }

    @Test
    @DisplayName("만료 시간이 지나지 않으면 사용할 수 있다")
    void notExpired() {
        //given
        final LocalDateTime now = EXPIRES_AT.minusSeconds(1);

        //when & then
        assertThat(inviteCode("4기모집").isExpired(now)).isFalse();
    }

    @Test
    @DisplayName("만료 시간이 되면 사용할 수 없다")
    void expired() {
        //when & then
        assertThat(inviteCode("4기모집").isExpired(EXPIRES_AT)).isTrue();
        assertThat(inviteCode("4기모집").isExpired(EXPIRES_AT.plusDays(1))).isTrue();
    }

    @Test
    @DisplayName("이름이 비어 있으면 생성할 수 없다")
    void blankLabel() {
        //when & then
        assertThatThrownBy(() -> inviteCode(" "))
            .isInstanceOf(GreedyBotException.class)
            .hasMessageContaining("초대 코드 이름을 입력해주세요");
    }

    @Test
    @DisplayName("이름에 저장 포맷 구분자가 들어가면 생성할 수 없다")
    void labelWithDelimiter() {
        //when & then
        assertThatThrownBy(() -> inviteCode("4기|모집"))
            .isInstanceOf(GreedyBotException.class)
            .hasMessageContaining("사용할 수 없습니다");
        assertThatThrownBy(() -> inviteCode("4기:모집"))
            .isInstanceOf(GreedyBotException.class)
            .hasMessageContaining("사용할 수 없습니다");
    }
}
