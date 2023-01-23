package ru.yuraender.discord.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.entity.BotGuild;
import ru.yuraender.discord.entity.BotUser;
import ru.yuraender.discord.registry.GuildRegistry;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand extends Command {

    private final Main instance = Main.getInstance();
    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

    public HelpCommand() {
        super("help", (Object) "Показывает это :)");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        Guild guild = event.getGuild();
        BotGuild botGuild = guildRegistry.get(guild.getIdLong());
        BotUser author = botGuild.getUser(event.getMember().getIdLong());

        List<String> lines = new ArrayList<>();
        lines.add("[Помощь] Список доступных команд:");

        List<String> used = new ArrayList<>();
        for (Command command : instance.getCommands().values()) {
            if (used.contains(command.getName())) {
                continue;
            }
            if (author.getMaxGroup().ordinal() >= command.getGroup().ordinal()) {
                String line = "[Помощь] %s%s — %s.";
                if (command.getAliases().size() == 0) {
                    lines.add(String.format(line, CommandHandler.PREFIX, command.getName(), command.getDescription()));
                } else {
                    String name = command.getName() + " (" + String.join(", ", command.getAliases()) + ")";
                    lines.add(String.format(line, CommandHandler.PREFIX, name, command.getDescription()));
                }
            }
            used.add(command.getName());
        }

        event.reply(String.join("\n", lines))
                .setEphemeral(true)
                .queue();
    }
}
