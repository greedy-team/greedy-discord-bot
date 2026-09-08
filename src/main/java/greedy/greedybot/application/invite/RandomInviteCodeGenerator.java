package greedy.greedybot.application.invite;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

@Component
public class RandomInviteCodeGenerator implements InviteCodeGenerator {

    // 사람이 옮겨 적을 때 헷갈리는 I, L, O, U 를 뺀 32 글자
    private static final String ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final String PREFIX = "GRD";
    private static final String BLOCK_DELIMITER = "-";
    private static final int BLOCK_SIZE = 5;
    private static final int BLOCK_COUNT = 2;

    private final SecureRandom secureRandom = new SecureRandom();

    // GRD-4X7K2-M9P3R 형태. 32^10 가지라 추측으로 맞히는 것은 사실상 불가능하다
    @Override
    public String generate() {
        final String blocks = IntStream.range(0, BLOCK_COUNT)
            .mapToObj(ignored -> randomBlock())
            .collect(Collectors.joining(BLOCK_DELIMITER));
        return PREFIX + BLOCK_DELIMITER + blocks;
    }

    private String randomBlock() {
        final StringBuilder block = new StringBuilder(BLOCK_SIZE);
        for (int i = 0; i < BLOCK_SIZE; i++) {
            block.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return block.toString();
    }
}
