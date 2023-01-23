package ru.yuraender.discord.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import ru.yuraender.discord.entity.enums.PermissionGroup;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CrazyMoveCommand extends Command {

    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public CrazyMoveCommand() {
        super("crazymove", PermissionGroup.ADMIN, (Object) "Ебашить пользователя по каналам", "cm");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        Guild guild = event.getGuild();
        User user = event.getOption("user").getAsUser();
        Member member = guild.getMemberById(user.getIdLong());
        if (!member.getVoiceState().inVoiceChannel()) {
            event.reply("[Ошибка] Пользователь не в голосовом канале.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        VoiceChannel oldChannel = member.getVoiceState().getChannel();
        service.scheduleAtFixedRate(new Runnable() {
            private int times = 5;

            @Override
            public void run() {
                if (--times >= 0) {
                    List<VoiceChannel> channels = guild.getVoiceChannels()
                            .stream()
                            .filter(c -> c.getParent() == null || c.getParent().getIdLong() != 665895668535001091L)
                            .collect(Collectors.toList());
                    guild.moveVoiceMember(
                            member,
                            channels.get(ThreadLocalRandom.current().nextInt(channels.size()))
                    ).queue();
                    try {
                        guild.moveVoiceMember(member, oldChannel).queueAfter(50L, TimeUnit.MILLISECONDS);
                    } catch (Exception ignored) {
                    }
                } else {
                    Thread.currentThread().interrupt();
                }
            }
        }, 0L, 100L, TimeUnit.MILLISECONDS);
        event.reply("Ебашим `" + user.getAsTag() + "` по каналам.").queue();
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData()
                .addOption(OptionType.USER, "user", "Пользователь", true);
    }
}
