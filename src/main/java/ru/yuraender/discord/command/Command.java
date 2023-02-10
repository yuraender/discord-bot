package ru.yuraender.discord.command;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.entity.enums.PermissionGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public abstract class Command {

    private final String name;
    private final PermissionGroup group;
    private final Object description;
    private final List<String> aliases = new ArrayList<>();

    public Command(String name, String... aliases) {
        this(name, PermissionGroup.UNTRUSTED, "", aliases);
    }

    public Command(String name, PermissionGroup group, String... aliases) {
        this(name, group, "", aliases);
    }

    public Command(String name, Object description, String... aliases) {
        this(name, PermissionGroup.UNTRUSTED, description, aliases);
    }

    public Command(String name, PermissionGroup group, Object description, String... aliases) {
        this.name = name;
        this.group = group;
        this.description = description;
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public abstract void execute(SlashCommandEvent event);

    public CommandData getCommandData() {
        return new CommandData(getName(), (String) getDescription());
    }

    public User getUserByMention(Message message) {
        return message.getMentionedUsers().get(0);
    }

    public TextChannel getChannelByMention(Message message) {
        return message.getMentionedChannels().get(0);
    }

    public void register() {
        Main.getInstance().getCommands().put(name, this);
        aliases.forEach(s -> {
            Main.getInstance().getCommands().put(s, this);
        });
    }
}
