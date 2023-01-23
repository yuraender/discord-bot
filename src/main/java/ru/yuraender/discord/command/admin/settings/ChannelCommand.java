package ru.yuraender.discord.command.admin.settings;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.command.Command;
import ru.yuraender.discord.entity.BotGuild;
import ru.yuraender.discord.entity.enums.PermissionGroup;
import ru.yuraender.discord.registry.GuildRegistry;

public class ChannelCommand extends Command {

    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

    public ChannelCommand() {
        super("channel", PermissionGroup.ADMIN, (Object) "Установить канал для сообщений");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        BotGuild guild = guildRegistry.get(event.getGuild().getIdLong());
        long id = event.getOption("channel").getAsLong();
        if (id != 0 && event.getGuild().getTextChannelById(id) == null) {
            event.reply("[Ошибка] Такого канала не существует на этом сервере.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        guild.setChannelId(id);
        guildRegistry.save(guild);
        event.reply("[Настройки] Вы изменили канал бота на этом сервере на `" + id + "`.").queue();
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData()
                .addOption(OptionType.CHANNEL, "channel", "Канал", true);
    }
}
