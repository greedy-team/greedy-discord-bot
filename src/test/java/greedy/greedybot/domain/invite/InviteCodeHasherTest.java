package greedy.greedybot.domain.invite;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InviteCodeHasherTest {

    private InviteCodeHasher inviteCodeHasher;

    @BeforeEach
    void setUp() {
        inviteCodeHasher = new InviteCodeHasher();
    }

    @Test
    @DisplayName("해시에는 코드 원문이 남지 않는다")
    void hashDoesNotContainRawCode() {
        //when
        final String hashed = inviteCodeHasher.hash("GRD-4X7K2-M9P3R");

        //then
        assertThat(hashed).doesNotContain("4X7K2", "M9P3R");
    }

    @Test
    @DisplayName("대소문자와 하이픈이 달라도 같은 코드로 인식한다")
    void matchesIgnoringCaseAndHyphen() {
        //given
        final String codeHash = inviteCodeHasher.hash("GRD-4X7K2-M9P3R");

        //when & then
        assertThat(inviteCodeHasher.matches("grd4x7k2m9p3r", codeHash)).isTrue();
        assertThat(inviteCodeHasher.matches("  GRD-4X7K2-M9P3R  ", codeHash)).isTrue();
    }

    @Test
    @DisplayName("다른 코드는 일치하지 않는다")
    void doesNotMatchOtherCode() {
        //given
        final String codeHash = inviteCodeHasher.hash("GRD-4X7K2-M9P3R");

        //when & then
        assertThat(inviteCodeHasher.matches("GRD-4X7K2-M9P3Q", codeHash)).isFalse();
    }
}
