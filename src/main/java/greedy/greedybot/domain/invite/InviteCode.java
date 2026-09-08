package greedy.greedybot.domain.invite;

import greedy.greedybot.common.exception.GreedyBotException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record InviteCode(
    String label,
    String codeHash,
    InvitableRole role,
    LocalDateTime expiresAt,
    String issuerId
) {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String FORBIDDEN_LABEL_CHARACTERS_REGEX = ".*[|:].*";

    public InviteCode {
        if (label == null || label.isBlank()) {
            throw new GreedyBotException("🚫 초대 코드 이름을 입력해주세요.");
        }
        // 채널 저장 포맷이 | 와 : 로 구분되므로 이름에 들어가면 저장한 값을 다시 읽을 수 없다
        if (label.matches(FORBIDDEN_LABEL_CHARACTERS_REGEX)) {
            throw new GreedyBotException("🚫 초대 코드 이름에는 | 와 : 를 사용할 수 없습니다: " + label);
        }
    }

    public boolean isExpired(final LocalDateTime now) {
        return !now.isBefore(expiresAt);
    }

    public String describe(final LocalDateTime now) {
        final String status = isExpired(now) ? "만료됨" : "사용 가능";
        return "`%s` · %s · %s 까지 · %s".formatted(label, role.label(), expiresAt.format(DISPLAY_FORMATTER), status);
    }
}
