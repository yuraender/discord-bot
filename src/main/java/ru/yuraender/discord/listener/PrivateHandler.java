package ru.yuraender.discord.listener;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.entity.BotGuild;
import ru.yuraender.discord.registry.GuildRegistry;

import java.util.HashSet;
import java.util.Set;

public class PrivateHandler extends ListenerAdapter {

    public static final Set<Long> channels = new HashSet<>();
    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        createChannel(event.getChannelJoined(), event.getMember());
        fixChannel(event);
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        createChannel(event.getChannelJoined(), event.getMember());
        fixChannel(event);
        deleteChannel(event.getChannelLeft());
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        fixChannel(event);
        deleteChannel(event.getChannelLeft());
    }

    private void createChannel(VoiceChannel channel, Member member) {
        Guild guild = channel.getGuild();
        BotGuild botGuild = guildRegistry.get(guild.getIdLong());
        if (channel.getIdLong() == botGuild.getPrivateChannelId()) {
            Category category = channel.getParent();
            category.createVoiceChannel(member.getEffectiveName())
                    .setUserlimit(2)
                    .syncPermissionOverrides()
                    .addPermissionOverride(
                            member,
                            Lists.newArrayList(Permission.MANAGE_CHANNEL),
                            Lists.newArrayList()
                    )
                    .queue(c -> {
                        System.out.println("Создание приватОчки " + member.getEffectiveName());
                        channels.add(c.getIdLong());
                        guild.moveVoiceMember(member, c).queue();
                    });
        }
    }

    private void fixChannel(GenericGuildVoiceUpdateEvent event) {
        VoiceChannel channelJoined = event.getChannelJoined(), channelLeft = event.getChannelLeft();
        if (channelJoined != null && channels.contains(channelJoined.getIdLong())
                && channelJoined.getMembers().size() > channelJoined.getUserLimit()) {
            channelJoined.getManager().setUserLimit(Math.min(99, channelJoined.getMembers().size())).queue();
        }
        if (channelLeft != null && channels.contains(channelLeft.getIdLong())
                && channelLeft.getMembers().size() + 1 == channelLeft.getUserLimit()) {
            channelLeft.getManager().setUserLimit(Math.max(1, channelLeft.getMembers().size())).queue();
        }
    }

    private void deleteChannel(VoiceChannel channel) {
        if (channels.contains(channel.getIdLong()) && channel.getMembers().size() == 0) {
            channel.delete().queue(s -> System.out.println("Удаление приватОчки " + channel.getName()));
        }
    }
}
