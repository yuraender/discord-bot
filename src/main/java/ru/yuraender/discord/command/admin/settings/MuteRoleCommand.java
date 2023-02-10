package ru.yuraender.discord.command.admin.settings;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.command.Command;
import ru.yuraender.discord.entity.BotGuild;
import ru.yuraender.discord.entity.enums.PermissionGroup;
import ru.yuraender.discord.registry.GuildRegistry;

public class MuteRoleCommand extends Command {

    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

    public MuteRoleCommand() {
        super("muterole", PermissionGroup.ADMIN, (Object) "Установить роль для мута");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        BotGuild guild = guildRegistry.get(event.getGuild().getIdLong());
        long id;
        if (event.getOption("role") != null) {
            id = event.getOption("role").getAsLong();
            if (event.getGuild().getRoleById(id) == null) {
                event.reply("[Ошибка] Такой роли не существует на этом сервере.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        } else {
            id = 0L;
        }
        guild.setMuteRoleId(id);
        guildRegistry.save(guild);
        event.reply("[Настройки] Вы изменили роль мута на этом сервере на `" + id + "`.").queue();
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData()
                .addOption(OptionType.ROLE, "role", "Роль", false);
    }
}
