package greedy.greedybot.presentation.jda.listener;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StatusCommandListener implements SlashCommandListener {

    private final Logger log = LoggerFactory.getLogger(StatusCommandListener.class);

    public String getCommandName() {
        return "status";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(this.getCommandName(), "Check the status of the bot")
                .addOption(OptionType.STRING, "ping", "Ping the bot")
                .addOption(OptionType.STRING, "ok", "Check the status of the bot")
                .addOption(OptionType.STRING, "hello", "Hello the bot");
    }

    @Override
    public void onAction(@NotNull final SlashCommandInteractionEvent event) {
        // TODO: Implement(move) the actions at the application layer
        String ping = event.getOption("ping").getAsString();
        String ok = event.getOption("ok").getAsString();
        String hello = event.getOption("hello").getAsString();

        log.info("ping: {}", ping);
        log.info("ok: {}", ok);
        log.info("hello: {}", hello);

        event.reply("Ok!").queue();
    }
}
