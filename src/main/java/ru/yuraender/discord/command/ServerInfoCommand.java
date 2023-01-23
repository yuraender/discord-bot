package ru.yuraender.discord.command;

import lombok.SneakyThrows;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerInfoCommand extends Command {

    public ServerInfoCommand() {
        super("serverinfo", (Object) "Показывает информацию о Minecraft сервере", "si");
    }

    @SneakyThrows
    @Override
    public void execute(SlashCommandEvent event) {
        String ip = event.getOption("ip").getAsString();
        int port = event.getOption("port") != null
                ? (int) event.getOption("port").getAsDouble()
                : 25565;
        JSONObject json = new JSONObject(this.getResponse(
                new URL("https://mcapi.us/server/status?ip=" + ip + "&port=" + port)));
        boolean status = json.getBoolean("online");
        String ping = json.has("ping") ? "\nПинг: " + json.getInt("ping") + "ms." : "";
        String online = json.has("players")
                ? "\nОнлайн на сервере: " + json.getJSONObject("players").getInt("now") + "/"
                + json.getJSONObject("players").getInt("max") + "." : "";
        String error = json.has("error") && !json.isNull("error") ? "\nОшибка: " + json.getString("error") : "";
        event.reply("Информация о `" + ip + ":" + port + "`:"
                + "\nСтатус: " + (status ? "включен" : "выключен") + "."
                + ping + online
                + (!error.equals("") ? !error.split(":")[1].equals(" ") ? error : "" : "")
        ).setEphemeral(true).queue();
    }

    private String getResponse(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64)" +
                " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String input;
        while ((input = br.readLine()) != null) {
            response.append(input);
        }
        br.close();
        return response.toString();
    }

    @Override
    public CommandData getCommandData() {
        return super.getCommandData()
                .addOption(OptionType.STRING, "ip", "IP-адрес сервера", true)
                .addOption(OptionType.INTEGER, "port", "Порт сервера", false);
    }
}
