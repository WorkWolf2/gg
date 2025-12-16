package com.minegolem.hypingNations.config;

import com.minegolem.hypingNations.HypingNations;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

@Getter
public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private NationConfig nationConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();

        this.nationConfig = new NationConfig(Objects.requireNonNull(config.getConfigurationSection("nation-system")));
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        loadConfigs();
    }
}
