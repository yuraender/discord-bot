package ru.yuraender.discord.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.util.configuration.file.FileConfiguration;
import ru.yuraender.discord.util.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class CustomConfig {

    private FileConfiguration config = null;
    private File configFile = null;
    private final String name;

    public CustomConfig(String name) {
        this.name = name;
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(Main.getDataFolder().toFile(), name + ".yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        Reader defConfigStream = new InputStreamReader(Resources.getResource(name + ".yml"));
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            config.setDefaults(defConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            Logger.getRootLogger().log(Level.FATAL, "Could not save config to " + configFile, ex);
        }
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(Main.getDataFolder().toFile(), name + ".yml");
        }
        if (!configFile.exists()) {
            Resources.saveResource(name + ".yml", false);
        }
    }
}
