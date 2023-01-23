package ru.yuraender.discord.command.tts;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import ru.yuraender.discord.command.Command;

import java.util.HashSet;
import java.util.Set;

public class TtsLockCommand extends Command {

    @Getter
    private static final Set<Long> locked = new HashSet<>();

    public TtsLockCommand() {
        super("ttslock", (Object) "Залочить произношение текста");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        Member member = event.getMember();
        if (!locked.contains(member.getIdLong())) {
            locked.add(member.getIdLong());
            event.reply("Разговорник залочен.")
                    .setEphemeral(true)
                    .queue();
        } else {
            locked.remove(member.getIdLong());
            event.reply("Разговорник разлочен.")
                    .setEphemeral(true)
                    .queue();
        }
    }
}
