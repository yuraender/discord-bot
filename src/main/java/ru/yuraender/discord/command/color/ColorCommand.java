package ru.yuraender.discord.command.color;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import ru.yuraender.discord.command.Command;

public class ColorCommand extends Command {

    public ColorCommand() {
        super("group", (Object) "Поставить цвет роли");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        //TODO: сделать когда-нибудь
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData();
    }
}
