package greedy.greedybot.presentation.jda.listener.birthday;

import greedy.greedybot.application.birthday.BirthdayApproveService;
import greedy.greedybot.presentation.jda.listener.GlobalButtonListener;
import greedy.greedybot.presentation.jda.listener.InCommandButtonInteractionListener;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import greedy.greedybot.presentation.jda.role.DiscordRoles;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BirthdayApprovalButtonListener implements GlobalButtonListener {
    private static final Logger log = LoggerFactory.getLogger(BirthdayApprovalButtonListener.class);

    private static final String APPROVE_ID = "approve";
    private static final String REJECT_ID = "reject";

    private final BirthdayApproveService birthdayApproveService;
    private final DiscordRoles discordRoles;

    public BirthdayApprovalButtonListener(BirthdayApproveService birthdayApproveService,
                                          DiscordRoles discordRoles) {
        this.birthdayApproveService = birthdayApproveService;
        this.discordRoles = discordRoles;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        log.info("11111111");
        if (event.isAcknowledged()) return;

        if (event.getMember() == null) {
            event.reply("멤버 정보를 찾을 수 없습니다.").setEphemeral(true).queue();
            return;
        }

        log.info("222222222");

        long adminRoleId = discordRoles.getRoleId(DiscordRole.DEVELOPER);
        boolean isAdmin = event.getMember().getRoles().stream()
                .anyMatch(role -> role.getIdLong() == adminRoleId);

        if (!isAdmin) {
            event.reply("권한이 없습니다. 관리자만 승인/거절이 가능합니다.").setEphemeral(true).queue();
            return;
        }

        log.info("3333333333");

        event.deferEdit().queue(hook -> {
            try {
                String[] parts = event.getComponentId().split(":");
                String componentId = parts[1];
                String resultMessage;
                if (componentId.equals(APPROVE_ID)) {
                    birthdayApproveService.approve(event.getMessage());
                    resultMessage = "✅ 승인 완료";
                } else // REJECT_ID
                {
                    //birthdayApproveService.reject(event.getMessage());
                    resultMessage = "❌ 거절 완료";
                }
                hook.editOriginal(resultMessage).setComponents().queue();
            } catch (Exception e) {
                log.error("버튼 처리 실패", e);
                hook.sendMessage("처리 중 오류가 발생했습니다.").setEphemeral(true).queue();
            }
        }, throwable -> {
            log.warn("이미 처리된 인터랙션이거나 지연 응답에 실패했습니다.");
        });
    }

    @Override
    public boolean isSupportingButtonId(String buttonId) {
        return buttonId.startsWith("birthday:");
    }

}
