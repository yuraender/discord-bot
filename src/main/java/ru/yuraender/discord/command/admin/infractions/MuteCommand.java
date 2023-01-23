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
import ru.yuraender.discord.entity.BotUser;
import ru.yuraender.discord.entity.enums.PermissionGroup;
import ru.yuraender.discord.registry.GuildRegistry;

import java.util.concurrent.TimeUnit;

public class MuteCommand extends Command {

    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

    public MuteCommand() {
        super("mute", PermissionGroup.ADMIN, (Object) "Замутить указанного пользователя");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        Guild guild = event.getGuild();
        BotGuild botGuild = guildRegistry.get(guild.getIdLong());
        BotUser author = botGuild.getUser(event.getUser().getIdLong());

        User user = event.getOption("user").getAsUser();
        if (author.getMaxGroup().ordinal() <= botGuild.getUser(user.getIdLong()).getMaxGroup().ordinal()) {
            event.reply("[Ошибка] У вас недостаточно прав, чтобы замутить этого пользователя.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            event.reply("[Ошибка] У меня недостаточно прав.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (!guild.getSelfMember().canInteract(guild.getMember(user))) {
            event.reply("[Ошибка] У меня недостаточно прав, чтобы замутить этого пользователя.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        Member member = guild.getMember(user);
        if (botGuild.getMuteRoleId() == 0) {
            event.reply("[Ошибка] Роль для мута не установлена на этом сервере.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        Role role = guild.getRoleById(botGuild.getMuteRoleId());
        guild.addRoleToMember(member, role).queue();
        guild.removeRoleFromMember(member, role)
                .queueAfter(event.getOption("time").getAsLong(), TimeUnit.MINUTES);
        event.reply(
                "[Мут] Вы успешно замутили `" + user.getAsTag() + "`."
        ).queue();
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData()
                .addOption(OptionType.USER, "user", "Пользователь", true)
                .addOption(OptionType.NUMBER, "time", "Время", true);
    }
}
