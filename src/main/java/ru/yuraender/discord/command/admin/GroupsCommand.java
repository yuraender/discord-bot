package ru.yuraender.discord.command.admin;

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

import java.util.stream.Collectors;

public class GroupsCommand extends Command {

    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

    public GroupsCommand() {
        super("groups", PermissionGroup.ADMIN, (Object) "Просмотреть группы пользователя");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        Guild guild = event.getGuild();
        BotGuild botGuild = guildRegistry.get(guild.getIdLong());

        User user = event.getOption("user").getAsUser();
        BotUser botUser = botGuild.getUser(user.getIdLong());
        String groups = botUser.getGroups()
                .stream()
                .map(Enum::toString)
                .collect(Collectors.joining(", "));
        event.reply("[Группы] Пользователь `" + user.getAsTag() + "` имеет: " + groups + ".")
                .setEphemeral(true)
                .queue();
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData()
                .addOption(OptionType.USER, "user", "Пользователь", true);
    }
}
