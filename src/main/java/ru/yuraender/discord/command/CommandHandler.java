package ru.yuraender.discord.command;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.entity.BotGuild;
import ru.yuraender.discord.entity.BotUser;
import ru.yuraender.discord.entity.enums.Permission;
import ru.yuraender.discord.registry.GuildRegistry;

public class CommandHandler extends ListenerAdapter {

    public static final String PREFIX = ".";
    private final GuildRegistry guildRegistry = Main.getInstance().getGuildRegistry();

//    @Override
//    public void onMessageReceived(MessageReceivedEvent event) {
//        MessageChannel channel = event.getChannel();
//        String message = event.getMessage().getContentRaw();
//        if (event.getAuthor().isBot() || !message.startsWith(PREFIX)) {
//            return;
//        }
//        BotGuild botGuild = guildRegistry.get(event.getGuild().getIdLong());
//        if (botGuild.getChannelId() != 0 && botGuild.getChannelId() != channel.getIdLong()) {
//            return;
//        }
//        String cmd = message.split(" ")[0].replace(PREFIX, "");
//        if (!Main.getInstance().getCommands().containsKey(cmd)) {
//            channel.sendMessage("[Ошибка] Неизвестная команда. Воспользуйтесь `" + PREFIX + "help`.").queue();
//            return;
//        }
//        Command command = Main.getInstance().getCommands().get(cmd);
//        if (!command.getName().equalsIgnoreCase(cmd) && !command.getAliases().contains(cmd)) {
//            return;
//        }
//        BotUser author = botGuild.getUser(event.getAuthor().getIdLong());
//        if (!author.hasPermission(Permission.ROOT)
//                && author.getMaxGroup().ordinal() < command.getGroup().ordinal()) {
//            channel.sendMessage("[Ошибка] Необходима группа " + command.getGroup().getName() + ".").queue();
//            return;
//        }
//        String[] args = message.split(" ");
//        command.execute(event, Arrays.copyOfRange(args, 1, args.length));
//    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        MessageChannel channel = event.getChannel();
        if (event.getUser().isBot()) {
            return;
        }
        BotGuild botGuild = guildRegistry.get(event.getGuild().getIdLong());
        if (botGuild.getChannelId() != 0 && botGuild.getChannelId() != channel.getIdLong()) {
            return;
        }
        Command command = Main.getInstance().getCommands().get(event.getName());
        if (!command.getName().equalsIgnoreCase(event.getName()) && !command.getAliases().contains(event.getName())) {
            return;
        }
        BotUser author = botGuild.getUser(event.getUser().getIdLong());
        if (!author.hasPermission(Permission.ROOT)
                && author.getMaxGroup().ordinal() < command.getGroup().ordinal()) {
            event.reply("[Ошибка] Необходима группа " + command.getGroup().getName() + ".")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        command.execute(event);
    }
}
