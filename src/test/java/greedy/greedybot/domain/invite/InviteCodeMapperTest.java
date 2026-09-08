package greedy.greedybot.domain.invite;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InviteCodeMapperTest {

    private InviteCodeMapper inviteCodeMapper;

    @BeforeEach
    void setUp() {
        inviteCodeMapper = new InviteCodeMapper();
    }

    @Test
    @DisplayName("초대 코드를 텍스트로 변환한다")
    void toTextEntity() {
        //given
        final InviteCode inviteCode = new InviteCode(
            "4기모집", "9f86d081", InvitableRole.MEMBER, LocalDateTime.of(2026, 10, 1, 0, 0), "1234");

        //when
        final String text = inviteCodeMapper.toTextEntity(inviteCode);

        //then
        assertThat(text).isEqualTo("label:4기모집|role:MEMBER|hash:9f86d081|expiresAt:2026-10-01T00:00:00|issuer:1234");
    }

    @Test
    @DisplayName("변환한 텍스트를 다시 초대 코드로 복원한다")
    void toEntity() {
        //given
        final InviteCode inviteCode = new InviteCode(
            "4기모집", "9f86d081", InvitableRole.COLLABORATOR, LocalDateTime.of(2026, 10, 1, 12, 30), "1234");

        //when
        final InviteCode restored = inviteCodeMapper.toEntity(inviteCodeMapper.toTextEntity(inviteCode));

        //then
        assertThat(restored).isEqualTo(inviteCode);
    }

    @Test
    @DisplayName("초대 코드 형식이 아닌 메세지는 걸러낸다")
    void isInviteCodeText() {
        //given
        final String inviteCodeText = "label:4기모집|role:MEMBER|hash:9f86d081|expiresAt:2026-10-01T00:00:00|issuer:1234";

        //when & then
        assertThat(inviteCodeMapper.isInviteCodeText(inviteCodeText)).isTrue();
        assertThat(inviteCodeMapper.isInviteCodeText("오늘 회의 3시에 합니다")).isFalse();
        assertThat(inviteCodeMapper.isInviteCodeText("label:4기모집|role:MEMBER")).isFalse();
    }
}
