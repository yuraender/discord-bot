package ru.yuraender.discord.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface DirectExecutable {

    void execute(MessageReceivedEvent event, String[] args);
}
