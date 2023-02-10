package ru.yuraender.discord.command.admin.settings;

import net.dv8tion.jda.api.entities.VoiceChannel;
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
        long id;
        if (event.getOption("channel") != null) {
            id = event.getOption("channel").getAsLong();
            VoiceChannel channel = event.getGuild().getVoiceChannelById(id);
            if (channel == null || channel.getParent() == null) {
                event.reply("[Ошибка] Такого канала не существует на этом сервере.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        } else {
            id = 0L;
        }
        guild.setPrivateChannelId(id);
        guildRegistry.save(guild);
        event.reply("[Настройки] Вы изменили канал приватных комнат на этом сервере на `" + id + "`.").queue();
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData()
                .addOption(OptionType.CHANNEL, "channel", "Канал", false);
    }
}
