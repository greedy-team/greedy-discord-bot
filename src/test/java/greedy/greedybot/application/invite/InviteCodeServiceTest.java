package greedy.greedybot.application.invite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.invite.InvitableRole;
import greedy.greedybot.domain.invite.InviteCode;
import greedy.greedybot.domain.invite.InviteCodeHasher;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InviteCodeServiceTest {

    private static final String RAW_CODE = "GRD-4X7K2-M9P3R";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 8, 10, 0);
    private static final String ISSUER_ID = "1234";
    private static final String USER_ID = "5678";

    private MockInviteCodeRepository inviteCodeRepository;
    private InviteCodeService inviteCodeService;

    @BeforeEach
    void setUp() {
        inviteCodeRepository = new MockInviteCodeRepository();
        inviteCodeService = new InviteCodeService(
            inviteCodeRepository,
            new MockInviteCodeGenerator(RAW_CODE),
            new InviteCodeHasher(),
            new RedeemAttemptLimiter());
    }

    private String createInviteCode() {
        return inviteCodeService.createInviteCode("4기모집", InvitableRole.MEMBER, 7, ISSUER_ID, NOW);
    }

    @Test
    @DisplayName("초대 코드를 발급하면 원문은 돌려주고 저장은 해시로 한다")
    void createInviteCodeStoresHashOnly() {
        //when
        final String rawCode = createInviteCode();

        //then
        assertThat(rawCode).isEqualTo(RAW_CODE);
        final InviteCode saved = inviteCodeRepository.findAll().getFirst();
        assertThat(saved.codeHash()).isNotEqualTo(RAW_CODE);
        assertThat(saved.expiresAt()).isEqualTo(NOW.plusDays(7));
        assertThat(saved.role()).isEqualTo(InvitableRole.MEMBER);
    }

    @Test
    @DisplayName("같은 이름으로는 초대 코드를 두 번 발급할 수 없다")
    void createInviteCodeDuplicatedLabel() {
        //given
        createInviteCode();

        //when & then
        assertThatThrownBy(this::createInviteCode)
            .isInstanceOf(GreedyBotException.class)
            .hasMessageContaining("이미 존재하는 초대 코드 이름입니다");
    }

    @Test
    @DisplayName("유효 기간이 범위를 벗어나면 발급할 수 없다")
    void createInviteCodeInvalidExpiresInDays() {
        //when & then
        assertThatThrownBy(() ->
            inviteCodeService.createInviteCode("4기모집", InvitableRole.MEMBER, 0, ISSUER_ID, NOW))
            .isInstanceOf(GreedyBotException.class)
            .hasMessageContaining("유효 기간은");
    }

    @Test
    @DisplayName("발급한 코드를 입력하면 부여할 역할을 돌려준다")
    void redeem() {
        //given
        final String rawCode = createInviteCode();

        //when
        final InviteCode redeemed = inviteCodeService.redeem(rawCode, USER_ID, NOW);

        //then
        assertThat(redeemed.role()).isEqualTo(InvitableRole.MEMBER);
        assertThat(redeemed.label()).isEqualTo("4기모집");
    }

    @Test
    @DisplayName("여러 사람이 같은 코드를 사용할 수 있다")
    void redeemSharedCode() {
        //given
        final String rawCode = createInviteCode();

        //when
        inviteCodeService.redeem(rawCode, USER_ID, NOW);

        //then
        assertThat(inviteCodeService.redeem(rawCode, "9999", NOW).label()).isEqualTo("4기모집");
    }

    @Test
    @DisplayName("존재하지 않는 코드는 사용할 수 없다")
    void redeemNotFound() {
        //given
        createInviteCode();

        //when & then
        assertThatThrownBy(() -> inviteCodeService.redeem("GRD-00000-00000", USER_ID, NOW))
            .isInstanceOf(GreedyBotException.class)
            .hasMessageContaining("유효하지 않은 초대 코드입니다");
    }

    @Test
    @DisplayName("만료된 코드는 사용할 수 없다")
    void redeemExpired() {
        //given
        final String rawCode = createInviteCode();

        //when & then
        assertThatThrownBy(() -> inviteCodeService.redeem(rawCode, USER_ID, NOW.plusDays(7)))
            .isInstanceOf(GreedyBotException.class)
            .hasMessageContaining("만료된 초대 코드입니다");
    }

    @Test
    @DisplayName("코드를 여러 번 틀리면 더 이상 시도할 수 없다")
    void redeemBlockedAfterRepeatedFailure() {
        //given
        createInviteCode();
        for (int attempt = 0; attempt < 5; attempt++) {
            assertThatThrownBy(() -> inviteCodeService.redeem("GRD-00000-00000", USER_ID, NOW))
                .hasMessageContaining("유효하지 않은 초대 코드입니다");
        }

        //when & then
        assertThatThrownBy(() -> inviteCodeService.redeem(RAW_CODE, USER_ID, NOW))
            .isInstanceOf(GreedyBotException.class)
            .hasMessageContaining("너무 많이 틀렸습니다");
    }

    @Test
    @DisplayName("시도 제한은 일정 시간이 지나면 풀린다")
    void redeemBlockReleasedAfterWindow() {
        //given
        final String rawCode = createInviteCode();
        for (int attempt = 0; attempt < 5; attempt++) {
            assertThatThrownBy(() -> inviteCodeService.redeem("GRD-00000-00000", USER_ID, NOW))
                .hasMessageContaining("유효하지 않은 초대 코드입니다");
        }

        //when & then
        assertThat(inviteCodeService.redeem(rawCode, USER_ID, NOW.plusMinutes(11)).label()).isEqualTo("4기모집");
    }

    @Test
    @DisplayName("폐기한 코드는 더 이상 사용할 수 없다")
    void revokeInviteCode() {
        //given
        final String rawCode = createInviteCode();

        //when
        inviteCodeService.revokeInviteCode("4기모집");

        //then
        assertThat(inviteCodeRepository.findAll()).isEmpty();
        assertThatThrownBy(() -> inviteCodeService.redeem(rawCode, USER_ID, NOW))
            .isInstanceOf(GreedyBotException.class)
            .hasMessageContaining("유효하지 않은 초대 코드입니다");
    }

    @Test
    @DisplayName("존재하지 않는 코드는 폐기할 수 없다")
    void revokeNotFound() {
        //when & then
        assertThatThrownBy(() -> inviteCodeService.revokeInviteCode("없는코드"))
            .isInstanceOf(GreedyBotException.class)
            .hasMessageContaining("존재하지 않는 초대 코드입니다");
    }
}
