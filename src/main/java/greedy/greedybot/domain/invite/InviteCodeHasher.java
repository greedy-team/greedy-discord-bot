package greedy.greedybot.domain.invite;

import greedy.greedybot.common.exception.GreedyBotException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

// 코드 원문은 저장하지 않고 해시만 저장한다.
// 채널을 볼 수 있는 사람도 코드를 알아낼 수 없어야 하기 때문이다.
@Component
public class InviteCodeHasher {

    private static final String ALGORITHM = "SHA-256";
    private static final String IGNORED_CHARACTERS_REGEX = "[\\s-]";

    public String hash(final String rawCode) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);
            final byte[] hashed = messageDigest.digest(normalize(rawCode).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new GreedyBotException("🚫 초대 코드를 처리하지 못했습니다.");
        }
    }

    // 입력한 코드의 해시와 저장된 해시를 비교한다. 타이밍 공격을 피하기 위해 isEqual 을 사용한다
    public boolean matches(final String rawCode, final String codeHash) {
        return MessageDigest.isEqual(
            hash(rawCode).getBytes(StandardCharsets.UTF_8),
            codeHash.getBytes(StandardCharsets.UTF_8));
    }

    // 대소문자나 하이픈을 다르게 입력해도 같은 코드로 인식되게 한다 (grd 4x7k2m9p3r == GRD-4X7K2-M9P3R)
    private String normalize(final String rawCode) {
        return rawCode.replaceAll(IGNORED_CHARACTERS_REGEX, "").toUpperCase(Locale.ROOT);
    }
}
