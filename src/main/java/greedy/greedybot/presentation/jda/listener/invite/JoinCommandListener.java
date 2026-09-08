package greedy.greedybot.presentation.jda.listener.invite;

import greedy.greedybot.application.invite.InviteCodeService;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.invite.InvitableRole;
import greedy.greedybot.domain.invite.InviteCode;
import greedy.greedybot.presentation.jda.listener.SlashCommandListener;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import greedy.greedybot.presentation.jda.role.DiscordRoles;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JoinCommandListener implements SlashCommandListener {

    private static final Logger log = LoggerFactory.getLogger(JoinCommandListener.class);

    private static final String CODE_OPTION = "code";

    private final InviteCodeService inviteCodeService;
    private final DiscordRoles discordRoles;

    public JoinCommandListener(final InviteCodeService inviteCodeService, final DiscordRoles discordRoles) {
        this.inviteCodeService = inviteCodeService;
        this.discordRoles = discordRoles;
    }

    @Override
    public String getCommandName() {
        return "join";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(this.getCommandName(), "초대 코드를 입력하고 채널 권한을 받습니다.")
            .addOption(OptionType.STRING, CODE_OPTION, "운영진에게 받은 초대 코드 (ex. GRD-4X7K2-M9P3R)", true)
            .setGuildOnly(true); // DM 으로 실행되면 멤버 정보가 없어 역할을 부여할 수 없다
    }

    @Override
    public void onAction(@NotNull final SlashCommandInteractionEvent event) {
        // 초대 코드 채널을 조회하므로 3초 안에 응답하지 못할 수 있다
        event.deferReply(true).queue();

        final String rawCode = getRequiredOption(event, CODE_OPTION).getAsString();
        final Member member = event.getMember();
        final InviteCode inviteCode = inviteCodeService.redeem(rawCode, member.getId(), LocalDateTime.now());
        final Role role = findRole(event.getGuild(), inviteCode.role());

        if (member.getRoles().contains(role)) {
            event.getHook().sendMessage("이미 가입되어 있습니다.").setEphemeral(true).queue();
            return;
        }

        grantRole(event.getGuild(), member, role);
        log.info("[JOINED BY INVITE CODE] : {} ({})", member.getId(), inviteCode.label());
        event.getHook()
            .sendMessage("✅ %s 역할이 부여되었습니다. 그리디에 오신 것을 환영합니다!".formatted(inviteCode.role().label()))
            .setEphemeral(true)
            .queue();
    }

    private Role findRole(final Guild guild, final InvitableRole invitableRole) {
        final long roleId = discordRoles.getRoleId(toDiscordRole(invitableRole));
        final Role role = guild.getRoleById(roleId);
        if (Objects.isNull(role)) {
            log.error("[INVITE ROLE NOT FOUND] : {} ({})", invitableRole.name(), roleId);
            throw new GreedyBotException("🚫 부여할 역할을 찾지 못했습니다. 운영진에게 문의해주세요.");
        }
        return role;
    }

    private DiscordRole toDiscordRole(final InvitableRole invitableRole) {
        return switch (invitableRole) {
            case MEMBER -> DiscordRole.MEMBER;
            case COLLABORATOR -> DiscordRole.COLLABORATOR;
        };
    }

    // 봇의 역할이 부여할 역할보다 아래에 있거나 역할 관리 권한이 없으면 실패한다.
    // 원인을 알 수 있어야 운영진이 바로 고칠 수 있으므로 메세지를 구분해서 보여준다
    private void grantRole(final Guild guild, final Member member, final Role role) {
        try {
            guild.addRoleToMember(member, role).complete();
        } catch (HierarchyException e) {
            log.error("[INVITE ROLE HIERARCHY] : {}", role.getName(), e);
            throw new GreedyBotException("🚫 봇의 역할이 %s 역할보다 아래에 있어 부여할 수 없습니다. 운영진에게 문의해주세요."
                .formatted(role.getName()));
        } catch (ErrorResponseException e) {
            if (e.getErrorResponse() == ErrorResponse.MISSING_PERMISSIONS) {
                log.error("[INVITE ROLE PERMISSION] : {}", role.getName(), e);
                throw new GreedyBotException("🚫 봇에게 역할 관리 권한이 없습니다. 운영진에게 문의해주세요.");
            }
            throw e;
        }
    }

    private OptionMapping getRequiredOption(final SlashCommandInteractionEvent event, final String optionName) {
        final OptionMapping option = event.getOption(optionName);
        if (Objects.isNull(option)) {
            log.warn("[EMPTY INVITE CODE OPTION] : {}", optionName);
            throw new GreedyBotException("🚫 " + optionName + " 정보가 입력 되지 않았습니다.");
        }
        return option;
    }

    // 아직 역할이 없는 사람이 사용하는 명령어이므로 역할을 검사하지 않는다
    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of();
    }
}
