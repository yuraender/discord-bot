package ru.yuraender.discord.command.admin.infractions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.command.Command;
import ru.yuraender.discord.entity.BotGuild;
import ru.yuraender.discord.entity.enums.PermissionGroup;
import ru.yuraender.discord.registry.GuildRegistry;

public class UnmuteCommand extends Command {

    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

    public UnmuteCommand() {
        super("unmute", PermissionGroup.ADMIN, (Object) "Размутить указанного пользователя");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        Guild guild = event.getGuild();
        BotGuild botGuild = guildRegistry.get(guild.getIdLong());
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            event.reply("[Ошибка] У меня недостаточно прав.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        User user = event.getOption("user").getAsUser();
        Member member = guild.getMember(user);
        if (botGuild.getMuteRoleId() == 0) {
            event.reply("[Ошибка] Роль для мута не установлена на этом сервере.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        Role role = guild.getRoleById(botGuild.getMuteRoleId());
        guild.removeRoleFromMember(member, role).queue();
        event.reply(
                "[Размут] Вы успешно размутили `" + user.getAsTag() + "`."
        ).queue();
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData()
                .addOption(OptionType.USER, "user", "Пользователь", true);
    }
}
