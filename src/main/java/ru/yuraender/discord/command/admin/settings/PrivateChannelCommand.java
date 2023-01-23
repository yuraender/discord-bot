package ru.yuraender.discord.command.admin.settings;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.command.Command;
import ru.yuraender.discord.entity.BotGuild;
import ru.yuraender.discord.entity.enums.PermissionGroup;
import ru.yuraender.discord.registry.GuildRegistry;

public class PrivateChannelCommand extends Command {

    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

    public PrivateChannelCommand() {
        super("privatechannel", PermissionGroup.ADMIN, (Object) "Установить канал для создания приватных комнат");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        BotGuild guild = guildRegistry.get(event.getGuild().getIdLong());
        GuildChannel channel;
        if (event.getOption("channel") != null) {
            channel = event.getOption("channel").getAsGuildChannel();
            guild.setPrivateChannelId(channel.getIdLong());
        } else {
            channel = null;
            guild.setPrivateChannelId(0L);
        }
        guildRegistry.save(guild);
        event.reply("[Настройки] Вы изменили канал приватных комнат на этом сервере на `" + channel + "`.").queue();
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData()
                .addOption(OptionType.CHANNEL, "channel", "Канал", false);
    }
}
