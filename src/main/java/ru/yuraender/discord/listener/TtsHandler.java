package ru.yuraender.discord.listener;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.command.tts.TtsCommand;
import ru.yuraender.discord.command.tts.TtsLockCommand;
import ru.yuraender.discord.entity.BotGuild;
import ru.yuraender.discord.registry.GuildRegistry;

public class TtsHandler extends ListenerAdapter {

    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        if (event.getAuthor().isBot()) {
            return;
        }
        BotGuild botGuild = guildRegistry.get(event.getGuild().getIdLong());
        if (botGuild.getChannelId() != 0 && botGuild.getChannelId() != channel.getIdLong()) {
            return;
        }

        if (TtsLockCommand.getLocked().contains(event.getAuthor().getIdLong())) {
            TtsCommand.tts(event, null);
        }
    }
}
