package ru.yuraender.discord.command.tts;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.command.Command;
import ru.yuraender.discord.entity.BotGuild;
import ru.yuraender.discord.entity.BotUser;
import ru.yuraender.discord.music.speechkit.YandexSpeechkitAPI;
import ru.yuraender.discord.registry.GuildRegistry;

public class TtsVoiceCommand extends Command {

    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

    public TtsVoiceCommand() {
        super("ttsvoice", (Object) "Устанавливает голос и его амплуа");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        BotGuild botGuild = guildRegistry.get(event.getGuild().getIdLong());
        Member member = event.getMember();
        BotUser botUser = botGuild.getUser(member.getIdLong());
        botUser.setVoice(YandexSpeechkitAPI.Voice
                .valueOf(event.getOption("voice").getAsString()));
        botUser.setEmotion(YandexSpeechkitAPI.Voice.Emotion
                .valueOf(event.getOption("emotion").getAsString()));
        guildRegistry.saveUser(botUser);
        event.reply("Установлен голос " + botUser.getVoice() + " и амплуа " + botUser.getEmotion() + ".")
                .setEphemeral(true)
                .queue();
    }

    @Override
    public CommandData getCommandData() {
        OptionData voices = new OptionData(OptionType.STRING, "voice", "Голос", true);
        for (YandexSpeechkitAPI.Voice voice : YandexSpeechkitAPI.Voice.values()) {
            voices.addChoice(voice.name().toLowerCase(), voice.name());
        }
        OptionData emotions = new OptionData(OptionType.STRING, "emotion", "Амплуа", true);
        for (YandexSpeechkitAPI.Voice.Emotion emotion : YandexSpeechkitAPI.Voice.Emotion.values()) {
            emotions.addChoice(emotion.name().toLowerCase(), emotion.name());
        }
        return super.getCommandData().addOptions(voices, emotions);
    }
}
