package ru.yuraender.discord.command.admin;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.EnumUtils;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.command.Command;
import ru.yuraender.discord.entity.BotGuild;
import ru.yuraender.discord.entity.BotUser;
import ru.yuraender.discord.entity.enums.PermissionGroup;
import ru.yuraender.discord.registry.GuildRegistry;

public class GroupCommand extends Command {

    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

    public GroupCommand() {
        super("group", PermissionGroup.ADMIN, (Object) "Изменить группы пользователя");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        Guild guild = event.getGuild();

        String a = event.getOption("action").getAsString();
        String g = event.getOption("group").getAsString();
        if (!EnumUtils.isValidEnum(PermissionGroup.Action.class, a.toUpperCase())) {
            event.reply("[Ошибка] Такого действия не существует.").queue();
            return;
        }
        if (!EnumUtils.isValidEnum(PermissionGroup.class, g.toUpperCase())) {
            event.reply("[Ошибка] Такой группы не существует.").queue();
            return;
        }
        PermissionGroup.Action action = PermissionGroup.Action.valueOf(a.toUpperCase());
        PermissionGroup group = PermissionGroup.valueOf(g.toUpperCase());
        if (group == PermissionGroup.UNTRUSTED) {
            event.reply("[Ошибка] С данной группой нельзя взаимодействовать.").queue();
            return;
        }

        User user = event.getOption("user").getAsUser();
        BotGuild botGuild = guildRegistry.get(guild.getIdLong());
        BotUser botUser = botGuild.getUser(user.getIdLong());
        if (action == PermissionGroup.Action.ADD && botUser.getGroups().contains(group)) {
            event.reply("[Ошибка] Пользователь уже имеет данную группу.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (action == PermissionGroup.Action.REMOVE && !botUser.getGroups().contains(group)) {
            event.reply("[Ошибка] Пользователь еще не имеет данной группы.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (action == PermissionGroup.Action.ADD) {
            botUser.getGroups().add(group);
            event.reply("[Группы] Вы выдали "
                    + "группу " + group.getName() + " пользователю "
                    + "`" + user.getAsTag() + "`.").queue();
        } else {
            botUser.getGroups().remove(group);
            event.reply("[Группы] Вы забрали "
                    + "группу " + group.getName() + " у пользователя "
                    + "`" + user.getAsTag() + "`.").queue();
        }
        guildRegistry.saveUser(botUser);
    }

    @Override
    public CommandData getCommandData() {
        OptionData actions = new OptionData(OptionType.STRING, "action", "Действие", true);
        for (PermissionGroup.Action action : PermissionGroup.Action.values()) {
            actions.addChoice(action.name().toLowerCase(), action.name());
        }
        OptionData groups = new OptionData(OptionType.STRING, "group", "Группа", true);
        for (PermissionGroup group : PermissionGroup.values()) {
            if (group == PermissionGroup.UNTRUSTED) {
                continue;
            }
            actions.addChoice(group.getName().toLowerCase(), group.name());
        }
        return super.getCommandData()
                .addOptions(
                        actions,
                        new OptionData(OptionType.USER, "user", "Пользователь", true),
                        groups
                );
    }
}
