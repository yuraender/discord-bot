package ru.yuraender.discord.command.admin.settings;

import net.dv8tion.jda.api.entities.Role;
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
        Role role;
        if (event.getOption("role") != null) {
            role = event.getOption("role").getAsRole();
            guild.setMuteRoleId(role.getIdLong());
        } else {
            role = null;
            guild.setMuteRoleId(0L);
        }
        guildRegistry.save(guild);
        event.reply("[Настройки] Вы изменили роль мута на этом сервере на `" + role + "`.").queue();
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData()
                .addOption(OptionType.ROLE, "role", "Роль", false);
    }
}
