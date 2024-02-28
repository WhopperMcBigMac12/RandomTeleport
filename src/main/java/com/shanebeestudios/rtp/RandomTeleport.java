package com.shanebeestudios.rtp;

import com.shanebeestudios.rtp.command.RTPCommand;
import com.shanebeestudios.rtp.config.Config;
import com.shanebeestudios.rtp.util.UpdateChecker;
import com.shanebeestudios.rtp.util.Utils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.exceptions.UnsupportedVersionException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class RandomTeleport extends JavaPlugin {

    private static RandomTeleport pluginInstance;
    private boolean commandApiCanLoad;
    private Config config;

    @Override
    public void onLoad() {
        try {
            CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(false));
            this.commandApiCanLoad = true;
        } catch (UnsupportedVersionException ignore) {
            this.commandApiCanLoad = false;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onEnable() {
        // Check if CommandAPI was loaded
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (!this.commandApiCanLoad) {
            Utils.log("&eIt appears the CommandAPI is not available on your server version.");
            Utils.log("&eThis is not a bug.");
            Utils.log("&eThis plugin will be updated when CommandAPI supports your server version.");
            Utils.log("&ePlugin will disable!");
            pluginManager.disablePlugin(this);
            return;
        }

        // Start loading plugin
        long start = System.currentTimeMillis();
        CommandAPI.onEnable();
        pluginInstance = this;
        Utils.log("Loading...");

        this.config = new Config(this);
        if (this.config.checkForUpdates()) {
            new UpdateChecker(this);
        }

        new RTPCommand(this);

        float finish = (float) (System.currentTimeMillis() - start) / 1000;
        Utils.log("&aSuccessfully enabled v%s &7in &b%.2f seconds", getDescription().getVersion(), finish);
    }

    @NotNull
    public Config getPluginConfig() {
        if (this.config == null) {
            throw new IllegalArgumentException("Config file not found!");
        }
        return config;
    }

    @SuppressWarnings("unused")
    public static RandomTeleport getPluginInstance() {
        if (pluginInstance == null) {
            throw new IllegalArgumentException("Plugin instance is null!");
        }
        return pluginInstance;
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }

}
