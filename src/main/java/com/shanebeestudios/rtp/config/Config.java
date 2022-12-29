package com.shanebeestudios.rtp.config;

import com.shanebeestudios.rtp.RandomTeleport;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Config {

    private final RandomTeleport plugin;
    private FileConfiguration config;
    private File configFile;

    public Config(RandomTeleport plugin) {
        this.plugin = plugin;
        loadConfigFile();
    }

    private void loadConfigFile() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        matchConfig();
        loadConfigs();
    }

    // Used to update config
    @SuppressWarnings("ConstantConditions")
    private void matchConfig() {
        try {
            boolean hasUpdated = false;
            InputStream stream = plugin.getResource(configFile.getName());
            assert stream != null;
            InputStreamReader is = new InputStreamReader(stream);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(is);
            for (String key : defConfig.getConfigurationSection("").getKeys(true)) {
                if (!config.contains(key)) {
                    config.set(key, defConfig.get(key));
                    hasUpdated = true;
                }
            }
            for (String key : config.getConfigurationSection("").getKeys(true)) {
                if (!defConfig.contains(key)) {
                    config.set(key, null);
                    hasUpdated = true;
                }
            }
            if (hasUpdated)
                config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Map<String, Integer> maxDistances = new HashMap<>();
    private final Map<String, Integer> maxY = new HashMap<>();
    private final Map<String, Integer> minY = new HashMap<>();
    private int maxRetries;

    @SuppressWarnings("ConstantConditions")
    private void loadConfigs() {
        for (String key : config.getConfigurationSection("teleporter.max_distance").getKeys(false)) {
            int value = config.getInt("teleporter.max_distance." + key);
            maxDistances.put(key, value);
        }
        for (String key : config.getConfigurationSection("teleporter.max_y").getKeys(false)) {
            int value = config.getInt("teleporter.max_y." + key);
            maxY.put(key, value);
        }
        for (String key : config.getConfigurationSection("teleporter.min_y").getKeys(false)) {
            int value = config.getInt("teleporter.min_y." + key);
            minY.put(key, value);
        }

        maxRetries = config.getInt("teleporter.max_retries");
    }

    public Map<String, Integer> getMaxDistances() {
        return maxDistances;
    }

    public Map<String, Integer> getMaxY() {
        return maxY;
    }

    public Map<String, Integer> getMinY() {
        return minY;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

}
