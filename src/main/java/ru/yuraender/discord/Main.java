package ru.yuraender.discord;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jooq.impl.SQLDataType;
import ru.yuraender.discord.command.*;
import ru.yuraender.discord.command.admin.GroupCommand;
import ru.yuraender.discord.command.admin.GroupsCommand;
import ru.yuraender.discord.command.admin.settings.ChannelCommand;
import ru.yuraender.discord.command.admin.settings.PrivateChannelCommand;
import ru.yuraender.discord.command.tts.TtsCommand;
import ru.yuraender.discord.command.tts.TtsLockCommand;
import ru.yuraender.discord.command.tts.TtsVoiceCommand;
import ru.yuraender.discord.entity.BotGuild;
import ru.yuraender.discord.listener.GuildHandler;
import ru.yuraender.discord.listener.PrivateHandler;
import ru.yuraender.discord.listener.TtsHandler;
import ru.yuraender.discord.music.GuildMusicManager;
import ru.yuraender.discord.music.speechkit.YandexSpeechkitAPI;
import ru.yuraender.discord.mysql.MySQL;
import ru.yuraender.discord.mysql.SQL;
import ru.yuraender.discord.registry.GuildRegistry;
import ru.yuraender.discord.util.CustomConfig;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.jooq.impl.DSL.*;

@Getter
public class Main {

    @Getter
    private static Main instance;
    private final JDA jda;
    private CustomConfig config;
    private CustomConfig mysqlConfig;
    private CustomConfig speechkitConfig;
    private MySQL sql;
    private GuildRegistry guildRegistry;
    private final Map<String, Command> commands = new HashMap<>();

    private AudioPlayerManager playerManager;
    private Map<Long, GuildMusicManager> musicManagers;
    private YandexSpeechkitAPI speechkitAPI;

    public Main() throws InterruptedException, LoginException {
        instance = this;
        config = new CustomConfig("config");
        config.saveDefaultConfig();
        mysqlConfig = new CustomConfig("mysql");
        mysqlConfig.saveDefaultConfig();
        speechkitConfig = new CustomConfig("speechkit");
        speechkitConfig.saveDefaultConfig();
        sql = new MySQL();
        createTables();
        guildRegistry = new GuildRegistry();

        JDABuilder bot = JDABuilder.createDefault(
                config.getConfig().getString("token"),
                EnumSet.of(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGES)
        );
        bot.setStatus(OnlineStatus.valueOf(config.getConfig().getString("status")));
        bot.enableCache(CacheFlag.VOICE_STATE);
        bot.setMemberCachePolicy(MemberCachePolicy.ALL);
        bot.setChunkingFilter(ChunkingFilter.ALL);

        initListeners(bot);
        jda = bot.build().awaitReady();
        initCommands();
        guildRegistry.loadAll(jda);

        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(playerManager);
        musicManagers = new HashMap<>();
        speechkitAPI = new YandexSpeechkitAPI();

        for (BotGuild botGuild : guildRegistry.getGuilds()) {
            if (botGuild.getPrivateChannelId() == 0) {
                continue;
            }
            VoiceChannel privateChannel = jda.getVoiceChannelById(botGuild.getPrivateChannelId());
            if (privateChannel == null) {
                continue;
            }
            Category category = privateChannel.getParent();
            if (category == null) {
                continue;
            }
            for (VoiceChannel channel : category.getVoiceChannels()) {
                if (channel.getIdLong() == privateChannel.getIdLong()) {
                    continue;
                }
                if (channel.getMembers().size() != 0) {
                    System.out.println("Создание приватОчки " + channel.getName());
                    PrivateHandler.channels.add(channel.getIdLong());
                } else {
                    channel.delete().queue();
                }
            }
        }
    }

    private void createTables() {
        SQL.getContext()
                .createTableIfNotExists("guilds")
                .columns(
                        field("id", SQLDataType.BIGINT.nullable(false)),
                        field("channel_id", SQLDataType.BIGINT.nullable(false)),
                        field("private_channel_id", SQLDataType.BIGINT.nullable(false)),
                        field("mute_role_id", SQLDataType.BIGINT.nullable(false))
                )
                .constraints(
                        primaryKey("id")
                )
                .execute();
        SQL.getContext()
                .createTableIfNotExists("users")
                .columns(
                        field("id", SQLDataType.BIGINT.nullable(false)),
                        field("server_id", SQLDataType.BIGINT.nullable(false)),
                        field("groups", SQLDataType.CLOB.nullable(false)),
                        field("permissions", SQLDataType.CLOB.nullable(false)),
                        field("voice", SQLDataType.NVARCHAR(10).nullable(false)),
                        field("emotion", SQLDataType.NVARCHAR(10).nullable(false))
                )
                .constraints(
                        primaryKey("id", "server_id"),
                        foreignKey("server_id").references("guilds", "id").onDeleteCascade()
                )
                .execute();
    }

    private void initCommands() {
//        new BanCommand().register();
//        new KickCommand().register();
//        new MuteCommand().register();
//        new TimeoutCommand().register();
//        new UnmuteCommand().register();
        new ChannelCommand().register();
//        new MuteRoleCommand().register();
        new PrivateChannelCommand().register();
        new GroupCommand().register();
        new GroupsCommand().register();
//        new VoiceBotCommand().register();
        new TtsCommand().register();
        new TtsLockCommand().register();
        new TtsVoiceCommand().register();
        new CrazyMoveCommand().register();
        new HelpCommand().register();
        new ServerInfoCommand().register();

        CommandListUpdateAction action = jda.updateCommands();
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            CommandData commandData = entry.getValue().getCommandData();
            if (commandData == null) {
                continue;
            }
            CommandData data = new CommandData(
                    entry.getKey(), commandData.getDescription()
            ).addOptions(commandData.getOptions());
            action = action.addCommands(data);
        }
        action.queue();
    }

    private void initListeners(JDABuilder bot) {
        bot.addEventListeners(new CommandHandler());
        bot.addEventListeners(new GuildHandler());
        bot.addEventListeners(new PrivateHandler());
        bot.addEventListeners(new TtsHandler());
    }

    public static void main(String[] args) throws InterruptedException, LoginException {
//        LibsUtil.loadLibs();
        new Main();
    }

    public static Path getDataFolder() {
        URL startupUrl = Main.class.getProtectionDomain().getCodeSource().getLocation();
        Path path;
        try {
            path = Paths.get(startupUrl.toURI());
        } catch (Exception ex) {
            try {
                path = Paths.get(new URL(startupUrl.getPath()).getPath());
            } catch (Exception ex1) {
                path = Paths.get(startupUrl.getPath());
            }
        }
        path = path.getParent();
        return path.resolve(path + File.separator + "DiscordBot");
    }
}
