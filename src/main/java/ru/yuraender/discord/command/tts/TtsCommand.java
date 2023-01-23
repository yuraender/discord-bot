package ru.yuraender.discord.command.tts;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.command.Command;
import ru.yuraender.discord.entity.BotUser;
import ru.yuraender.discord.music.GuildMusicManager;
import ru.yuraender.discord.music.speechkit.YandexSpeechkitAPI;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TtsCommand extends Command {

    public static final long SHINIGAMI_03 = 241974539267997697L;

    private static final Main instance = Main.getInstance();
    private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    private static Future<?> lastScheduler;

    public TtsCommand() {
        super("tts", (Object) "Произнести текст в канале");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        tts(null, event);
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData()
                .addOption(OptionType.STRING, "text", "Текст", true)
                .addOption(OptionType.CHANNEL, "channel", "Канал", false);
    }

    public static void tts(MessageReceivedEvent messageEvent, SlashCommandEvent slashEvent) {
        Member member = messageEvent != null ? messageEvent.getMember() : slashEvent.getMember();
        VoiceChannel voiceChannel;
        if (messageEvent != null) {
            voiceChannel = member.getVoiceState().getChannel();
        } else {
            if (slashEvent.getOption("channel") != null) {
                voiceChannel = (VoiceChannel) slashEvent.getOption("channel").getAsGuildChannel();
            } else {
                voiceChannel = member.getVoiceState().getChannel();
            }
        }
        if (voiceChannel == null) {
            if (messageEvent != null) {
                messageEvent.getChannel().sendMessage("[Ошибка] Вы не находитесь в канале.").queue();
            } else {
                slashEvent.reply("[Ошибка] Вы не находитесь в канале.")
                        .setEphemeral(true)
                        .queue();
            }
            return;
        }

        if (voiceChannel.getMembers().stream().anyMatch(m -> m.getIdLong() == SHINIGAMI_03)) {
            if (messageEvent != null) {
                messageEvent.getChannel().sendMessage("[Анти-Шинигами] Ето пизда...").queue();
            } else {
                slashEvent.reply("[Анти-Шинигами] Ето пизда...")
                        .setEphemeral(true)
                        .queue();
            }
            return;
        }
//        if (PrivateHandler.channels.contains(voiceChannel.getIdLong())
//                && voiceChannel != member.getVoiceState().getChannel()
//                && !member.hasPermission(voiceChannel, Permission.MANAGE_CHANNEL)
//                && botUser.getMaxGroup() != PermissionGroup.ADMIN) {
//            event.reply("[Ошибка] Вы не можете использовать эту команду для приватных комнат.")
//                    .setEphemeral(true)
//                    .queue();
//            return;
//        }

        YandexSpeechkitAPI speechkitAPI = instance.getSpeechkitAPI();
        String text = messageEvent != null
                ? messageEvent.getMessage().getContentRaw()
                : slashEvent.getOption("text").getAsString();
        if (speechkitAPI.getSymbolsLeft() - text.length() < 0) {
            if (messageEvent != null) {
                messageEvent.getChannel()
                        .sendMessage("[Ошибка] Бот не может произнести больше "
                                + speechkitAPI.getSymbolsLeft() + " символов до обновления.")
                        .queue();
            } else {
                slashEvent.reply("[Ошибка] Бот не может произнести больше "
                                + speechkitAPI.getSymbolsLeft() + " символов до обновления.")
                        .setEphemeral(true)
                        .queue();
            }
            return;
        }
        int limit = speechkitAPI.getLimit().computeIfAbsent(member.getIdLong(), id -> 3000);
        if (limit - text.length() < 0) {
            if (messageEvent != null) {
                messageEvent.getChannel()
                        .sendMessage("[Ошибка] У вас осталось " + limit + " символов до обновления.")
                        .queue();
            } else {
                slashEvent.reply("[Ошибка] У вас осталось " + limit + " символов до обновления.")
                        .setEphemeral(true)
                        .queue();
            }
            return;
        }

        BotUser botUser = instance.getGuildRegistry()
                .get(messageEvent != null ? messageEvent.getGuild().getIdLong() : slashEvent.getGuild().getIdLong())
                .getUser(member.getIdLong());
        speechkitAPI.setSymbolsLeft(speechkitAPI.getSymbolsLeft() - text.length());
        speechkitAPI.getLimit().put(member.getIdLong(), limit - text.length());
        File response = speechkitAPI.getResponse(text, botUser.getVoice(), botUser.getEmotion());
        loadAndPlay(voiceChannel, response.toPath().toString());

        if (messageEvent != null) {
            messageEvent.getMessage().delete().queue();
            messageEvent.getChannel()
                    .sendMessage(messageEvent.getAuthor().getAsTag() + " говорит `" + text + "`.")
                    .queue();
        } else {
            slashEvent.reply(slashEvent.getUser().getAsTag() + " говорит `" + text + "`.").queue();
        }
    }

    private static void loadAndPlay(VoiceChannel channel, String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        instance.getPlayerManager().loadItem(trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.getGuild().getAudioManager().openAudioConnection(channel);
                musicManager.scheduler.queue(track);

                if (lastScheduler != null) {
                    lastScheduler.cancel(true);
                }
                lastScheduler = service.schedule(() -> {
                    channel.getGuild().getAudioManager().closeAudioConnection();
                }, track.getDuration() + 120000L, TimeUnit.MILLISECONDS);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        });
    }

    private static synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        GuildMusicManager musicManager = instance.getMusicManagers().get(guild.getIdLong());
        if (musicManager == null) {
            musicManager = new GuildMusicManager(instance.getPlayerManager());
            instance.getMusicManagers().put(guild.getIdLong(), musicManager);
        }
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }
}
