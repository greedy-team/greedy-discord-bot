package greedy.greedybot.presentation.jda.listener.invite;

import greedy.greedybot.application.invite.InviteCodeService;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.invite.InvitableRole;
import greedy.greedybot.domain.invite.InviteCode;
import greedy.greedybot.presentation.jda.listener.SlashCommandListener;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InviteCodeCommandListener implements SlashCommandListener {

    private static final Logger log = LoggerFactory.getLogger(InviteCodeCommandListener.class);

    private static final String CREATE_SUBCOMMAND = "create";
    private static final String LIST_SUBCOMMAND = "list";
    private static final String REVOKE_SUBCOMMAND = "revoke";

    private static final String LABEL_OPTION = "label";
    private static final String ROLE_OPTION = "role";
    private static final String EXPIRES_IN_DAYS_OPTION = "expires-in-days";

    private final InviteCodeService inviteCodeService;

    public InviteCodeCommandListener(final InviteCodeService inviteCodeService) {
        this.inviteCodeService = inviteCodeService;
    }

    @Override
    public String getCommandName() {
        return "invite-code";
    }

    @Override
    public SlashCommandData getCommandData() {
        final OptionData role = new OptionData(OptionType.STRING, ROLE_OPTION, "부여할 역할", true);
        Arrays.stream(InvitableRole.values())
            .forEach(invitableRole -> role.addChoice(invitableRole.label(), invitableRole.name()));

        final SubcommandData create = new SubcommandData(CREATE_SUBCOMMAND, "초대 코드 발급")
            .addOption(OptionType.STRING, LABEL_OPTION, "코드 이름 (ex. 4기모집)", true)
            .addOptions(role)
            .addOption(OptionType.INTEGER, EXPIRES_IN_DAYS_OPTION, "유효 기간 (일 단위, ex. 7)", true);

        final SubcommandData list = new SubcommandData(LIST_SUBCOMMAND, "발급된 초대 코드 목록");

        final SubcommandData revoke = new SubcommandData(REVOKE_SUBCOMMAND, "초대 코드 폐기")
            .addOption(OptionType.STRING, LABEL_OPTION, "폐기할 코드 이름", true);

        return Commands.slash(this.getCommandName(), "초대 코드 관리")
            .addSubcommands(create, list, revoke)
            .setGuildOnly(true);
    }

    @Override
    public void onAction(@NotNull final SlashCommandInteractionEvent event) {
        // 초대 코드 채널을 조회하므로 3초 안에 응답하지 못할 수 있다
        event.deferReply(true).queue();

        final String subcommand = event.getSubcommandName();
        switch (subcommand) {
            case CREATE_SUBCOMMAND -> createInviteCode(event);
            case LIST_SUBCOMMAND -> listInviteCodes(event);
            case REVOKE_SUBCOMMAND -> revokeInviteCode(event);
            case null, default -> throw new GreedyBotException("🚫 지원하지 않는 초대 코드 명령어입니다: " + subcommand);
        }
    }

    private void createInviteCode(final SlashCommandInteractionEvent event) {
        final String label = getRequiredOption(event, LABEL_OPTION).getAsString();
        final InvitableRole role = InvitableRole.from(getRequiredOption(event, ROLE_OPTION).getAsString());
        final int expiresInDays = getRequiredOption(event, EXPIRES_IN_DAYS_OPTION).getAsInt();

        final String rawCode = inviteCodeService.createInviteCode(
            label, role, expiresInDays, event.getUser().getId(), LocalDateTime.now());

        // 코드는 해시로만 저장되어 다시 볼 수 없으므로 여기서 반드시 안내한다
        event.getHook().sendMessage("""
            ✅ 초대 코드를 발급했습니다!
            - 코드: ||%s||
            - 이름: `%s`
            - 역할: %s
            - 유효 기간: %d일

            ⚠️ 이 코드는 다시 확인할 수 없습니다. 지금 복사해두세요. 잃어버렸다면 `/invite-code revoke` 후 다시 발급해주세요.
            """.formatted(rawCode, label.trim(), role.label(), expiresInDays))
            .setEphemeral(true)
            .queue();
    }

    private void listInviteCodes(final SlashCommandInteractionEvent event) {
        final List<InviteCode> inviteCodes = inviteCodeService.findAll();
        if (inviteCodes.isEmpty()) {
            event.getHook().sendMessage("발급된 초대 코드가 없습니다. `/invite-code create` 로 발급해주세요.")
                .setEphemeral(true)
                .queue();
            return;
        }

        final LocalDateTime now = LocalDateTime.now();
        final String announcement = inviteCodes.stream()
            .map(inviteCode -> "- " + inviteCode.describe(now))
            .collect(Collectors.joining("\n"));
        event.getHook().sendMessage("**[발급된 초대 코드]**\n" + announcement).setEphemeral(true).queue();
    }

    private void revokeInviteCode(final SlashCommandInteractionEvent event) {
        final String label = getRequiredOption(event, LABEL_OPTION).getAsString();

        final InviteCode inviteCode = inviteCodeService.revokeInviteCode(label);

        log.info("[INVITE CODE REVOKED BY] : {}", event.getUser().getId());
        event.getHook().sendMessage("✅ 초대 코드를 폐기했습니다: `%s`".formatted(inviteCode.label()))
            .setEphemeral(true)
            .queue();
    }

    private OptionMapping getRequiredOption(final SlashCommandInteractionEvent event, final String optionName) {
        final OptionMapping option = event.getOption(optionName);
        if (Objects.isNull(option)) {
            log.warn("[EMPTY INVITE CODE OPTION] : {}", optionName);
            throw new GreedyBotException("🚫 " + optionName + " 정보가 입력 되지 않았습니다.");
        }
        return option;
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.MAINTAINER, DiscordRole.DEVELOPER);
    }
}
