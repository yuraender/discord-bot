package ru.yuraender.discord.command.admin.infractions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
import net.dv8tion.jda.internal.utils.Checks;
import net.dv8tion.jda.internal.utils.Helpers;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.command.Command;
import ru.yuraender.discord.entity.BotGuild;
import ru.yuraender.discord.entity.BotUser;
import ru.yuraender.discord.entity.enums.PermissionGroup;
import ru.yuraender.discord.registry.GuildRegistry;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.internal.utils.PermissionUtil.getEffectivePermission;

public class TimeoutCommand extends Command {

    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

    public TimeoutCommand() {
        super("timeout", PermissionGroup.ADMIN, (Object) "Запиздить указанного пользователя");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        Guild guild = event.getGuild();
        BotGuild botGuild = guildRegistry.get(guild.getIdLong());
        BotUser author = botGuild.getUser(event.getUser().getIdLong());

        User user = event.getOption("user").getAsUser();
        if (author.getMaxGroup().ordinal() <= botGuild.getUser(user.getIdLong()).getMaxGroup().ordinal()) {
            event.reply("[Ошибка] У вас недостаточно прав, чтобы запиздить этого пользователя.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (!checkPermission(guild.getSelfMember(), 40)) {
            event.reply("[Ошибка] У меня недостаточно прав.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        Member member = guild.getMember(user);
        if (!guild.getSelfMember().canInteract(member)) {
            event.reply("[Ошибка] У меня недостаточно прав, чтобы запиздить этого пользователя.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        timeout(member, 365L, TimeUnit.DAYS).queue();
        event.reply(
                "[Таймаут] Вы успешно запиздили `" + user.getAsTag() + "`."
        ).queue();
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData()
                .addOption(OptionType.USER, "user", "Пользователь", true)
                .addOption(OptionType.STRING, "reason", "Причина", true);
    }

    private boolean checkPermission(Member member, long offset) {
        Checks.notNull(member, "Member");
        long effectivePerms = getEffectivePermission(member);
        long timeoutPerm = 1L << offset;
        return (effectivePerms & Permission.ADMINISTRATOR.getRawValue()) == Permission.ADMINISTRATOR.getRawValue()
                || (effectivePerms & timeoutPerm) == timeoutPerm;
    }

    private AuditableRestActionImpl<Void> timeout(Member member, long amount, TimeUnit unit) {
        OffsetDateTime date = Helpers.toOffset(System.currentTimeMillis() + unit.toMillis(amount));
        DataObject body = DataObject.empty().put("communication_disabled_until", date.toString());
        Route.CompiledRoute route = Route.Guilds.MODIFY_MEMBER.compile(member.getGuild().getId(), member.getId());
        return new AuditableRestActionImpl<>(Main.getInstance().getJda(), route, body);
    }
}
