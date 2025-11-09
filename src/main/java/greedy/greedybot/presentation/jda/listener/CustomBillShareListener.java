package greedy.greedybot.presentation.jda.listener;

import greedy.greedybot.application.billshare.BillShareService;
import greedy.greedybot.application.billshare.CustomBillShareResponse;
import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.billshare.BankInfo;
import greedy.greedybot.domain.billshare.CustomBillShare;
import greedy.greedybot.presentation.jda.role.DiscordRole;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CustomBillShareListener implements AutoCompleteInteractionListener {

    private static final Logger log = LoggerFactory.getLogger(CustomBillShareListener.class);
    private final BillShareService billShareService;

    @Value("${discord.billshare_channel_id}")
    private Long allowedChannelId;

    public CustomBillShareListener(BillShareService billShareService) {
        this.billShareService = billShareService;
    }

    @Override
    public String getCommandName() {
        return "billshare-custom";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(this.getCommandName(), "일반 정산하기!")
            .addOption(OptionType.STRING, "text", "정산 요청할 멤버들과 각각의 결제 금액을 자유로운 형태로 입력하세요.", true)
            .addOption(OptionType.STRING, "bank", "정산받을 은행명을 입력해주세요", true, true)
            .addOption(OptionType.STRING, "accountnumber", "정산받을 계좌번호를 입력해주세요 (숫자만 공백없이 입력)", true);
    }

    @Override
    public void onAction(final @NotNull SlashCommandInteractionEvent event) {
        validateAllowedChannel(event);

        final String text = event.getOption("text").getAsString();
        final String bankInput = event.getOption("bank").getAsString();
        final String accountNumber = event.getOption("accountnumber").getAsString();

        final BankInfo bankInfo = BANK_INFO_TO_ENUM.get(bankInput);
        if (bankInfo == null) {
            throw new GreedyBotException("❌ 알 수 없는 은행명입니다: " + bankInput);
        }

        String memberName = text.trim();
        int memberCount = memberName.split("\\s+").length;

        final CustomBillShare customBillShare = new CustomBillShare(
            text,
            bankInfo,
            accountNumber
        );
        log.info("✅ 메시지가 전송되었습니다.");

        final CustomBillShareResponse response = billShareService.customBillShare(customBillShare);

        event.reply(response.toDiscordMessage()).queue();
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull final CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals(getCommandName()) || !event.getFocusedOption().getName().equals("bank")) {
            return;
        }

        List<Command.Choice> choices = BANK_INFO_TO_ENUM.keySet().stream()
            .filter(name -> name.startsWith(event.getFocusedOption().getValue()))
            .map(name -> new Command.Choice(name, name))
            .toList();

        event.replyChoices(choices).queue();
        log.info("[✅ BANK AUTOCOMPLETE SUCCESS]");
    }

    private static final Map<String, BankInfo> BANK_INFO_TO_ENUM = Arrays.stream(BankInfo.values())
        .collect(Collectors.toMap(BankInfo::getBankName, bankInfo -> bankInfo));

    private void validateAllowedChannel(final @NotNull SlashCommandInteractionEvent event) {
        if (event.getChannel().getIdLong() != allowedChannelId) {
            log.warn("[NOT ALLOWED CHANNEL COMMAND]: {}", event.getUser().getEffectiveName());
            throw new GreedyBotException("정산하기는 현재 채널에서 사용할 수 없습니다");
        }
    }

    @Override
    public Set<DiscordRole> allowedRoles() {
        return Set.of(DiscordRole.MEMBER, DiscordRole.COLLABORATOR, DiscordRole.LEAD);
    }
}
