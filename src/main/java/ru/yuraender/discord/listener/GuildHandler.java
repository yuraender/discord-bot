package ru.yuraender.discord.listener;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.entity.BotUser;
import ru.yuraender.discord.registry.GuildRegistry;

public class GuildHandler extends ListenerAdapter {

    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        guildRegistry.create(event.getGuild());
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();
        BotUser botUser = guildRegistry.get(guild.getIdLong()).getUser(event.getUser().getIdLong());
        guildRegistry.removeUser(botUser, true);
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        guildRegistry.delete(guildRegistry.get(event.getGuild().getIdLong()));
    }
}
