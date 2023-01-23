package ru.yuraender.discord.command.admin.infractions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
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

public class BanCommand extends Command {

    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

    public BanCommand() {
        super("ban", PermissionGroup.ADMIN, (Object) "Забанить указанного пользователя");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        Guild guild = event.getGuild();
        BotGuild botGuild = guildRegistry.get(guild.getIdLong());
        BotUser author = botGuild.getUser(event.getUser().getIdLong());

        User user = event.getOption("user").getAsUser();
        if (author.getMaxGroup().ordinal() <= botGuild.getUser(user.getIdLong()).getMaxGroup().ordinal()) {
            event.reply("[Ошибка] У вас недостаточно прав, чтобы забанить этого пользователя.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (!guild.getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
            event.reply("[Ошибка] У меня недостаточно прав.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (!guild.getSelfMember().canInteract(guild.getMember(user))) {
            event.reply("[Ошибка] У меня недостаточно прав, чтобы забанить этого пользователя.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        guild.ban(user, 0, event.getOption("reason").getAsString()).queue();
        event.reply(
                "[Бан] Вы успешно забанили `" + user.getAsTag() + "`."
        ).queue();
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData()
                .addOption(OptionType.USER, "user", "Пользователь", true)
                .addOption(OptionType.STRING, "reason", "Причина", true);
    }
}
