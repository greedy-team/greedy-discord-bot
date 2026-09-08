package greedy.greedybot.application.invite;

import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.invite.InvitableRole;
import greedy.greedybot.domain.invite.InviteCode;
import greedy.greedybot.domain.invite.InviteCodeHasher;
import greedy.greedybot.domain.invite.InviteCodeRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InviteCodeService {

    private static final Logger log = LoggerFactory.getLogger(InviteCodeService.class);

    private static final int MIN_EXPIRES_IN_DAYS = 1;
    private static final int MAX_EXPIRES_IN_DAYS = 365;

    private final InviteCodeRepository inviteCodeRepository;
    private final InviteCodeGenerator inviteCodeGenerator;
    private final InviteCodeHasher inviteCodeHasher;
    private final RedeemAttemptLimiter redeemAttemptLimiter;

    public InviteCodeService(final InviteCodeRepository inviteCodeRepository,
                             final InviteCodeGenerator inviteCodeGenerator,
                             final InviteCodeHasher inviteCodeHasher,
                             final RedeemAttemptLimiter redeemAttemptLimiter) {
        this.inviteCodeRepository = inviteCodeRepository;
        this.inviteCodeGenerator = inviteCodeGenerator;
        this.inviteCodeHasher = inviteCodeHasher;
        this.redeemAttemptLimiter = redeemAttemptLimiter;
    }

    // 코드 원문은 해시로만 저장하므로 여기서 돌려주는 값이 코드를 볼 수 있는 유일한 기회다
    public String createInviteCode(final String label,
                                   final InvitableRole role,
                                   final int expiresInDays,
                                   final String issuerId,
                                   final LocalDateTime now) {
        final String trimmedLabel = label.trim();
        validateExpiresInDays(expiresInDays);
        validateNotDuplicated(trimmedLabel);

        final String rawCode = inviteCodeGenerator.generate();
        final InviteCode inviteCode = new InviteCode(
            trimmedLabel,
            inviteCodeHasher.hash(rawCode),
            role,
            now.plusDays(expiresInDays),
            issuerId
        );

        inviteCodeRepository.saveInviteCode(inviteCode);
        log.info("[INVITE CODE CREATED] : {} ({}, {}일)", trimmedLabel, role.name(), expiresInDays);
        return rawCode;
    }

    public InviteCode redeem(final String rawCode, final String userId, final LocalDateTime now) {
        redeemAttemptLimiter.validateNotBlocked(userId, now);

        final Optional<InviteCode> found = inviteCodeRepository.findAll().stream()
            .filter(inviteCode -> inviteCodeHasher.matches(rawCode, inviteCode.codeHash()))
            .findAny();
        if (found.isEmpty()) {
            redeemAttemptLimiter.recordFailure(userId, now);
            log.warn("[INVITE CODE NOT MATCHED] : {}", userId);
            throw new GreedyBotException("🚫 유효하지 않은 초대 코드입니다.");
        }

        final InviteCode inviteCode = found.get();
        if (inviteCode.isExpired(now)) {
            redeemAttemptLimiter.recordFailure(userId, now);
            log.warn("[INVITE CODE EXPIRED] : {} ({})", inviteCode.label(), userId);
            throw new GreedyBotException("🚫 만료된 초대 코드입니다. 운영진에게 문의해주세요.");
        }

        redeemAttemptLimiter.reset(userId);
        log.info("[INVITE CODE REDEEMED] : {} ({})", inviteCode.label(), userId);
        return inviteCode;
    }

    public InviteCode revokeInviteCode(final String label) {
        final InviteCode inviteCode = getByLabel(label.trim());

        inviteCodeRepository.deleteByLabel(inviteCode.label());
        log.info("[INVITE CODE REVOKED] : {}", inviteCode.label());
        return inviteCode;
    }

    public List<InviteCode> findAll() {
        return inviteCodeRepository.findAll().stream()
            .sorted(Comparator.comparing(InviteCode::expiresAt).reversed())
            .toList();
    }

    private InviteCode getByLabel(final String label) {
        return inviteCodeRepository.findAll().stream()
            .filter(inviteCode -> inviteCode.label().equals(label))
            .findAny()
            .orElseThrow(() -> new GreedyBotException("🚫 존재하지 않는 초대 코드입니다: " + label));
    }

    private void validateExpiresInDays(final int expiresInDays) {
        if (expiresInDays < MIN_EXPIRES_IN_DAYS || expiresInDays > MAX_EXPIRES_IN_DAYS) {
            throw new GreedyBotException(
                "🚫 유효 기간은 %d일 이상 %d일 이하여야 합니다.".formatted(MIN_EXPIRES_IN_DAYS, MAX_EXPIRES_IN_DAYS));
        }
    }

    private void validateNotDuplicated(final String label) {
        final boolean duplicated = inviteCodeRepository.findAll().stream()
            .anyMatch(inviteCode -> inviteCode.label().equals(label));
        if (duplicated) {
            log.warn("[DUPLICATED INVITE CODE LABEL] : {}", label);
            throw new GreedyBotException("🚫 이미 존재하는 초대 코드 이름입니다: " + label);
        }
    }
}
