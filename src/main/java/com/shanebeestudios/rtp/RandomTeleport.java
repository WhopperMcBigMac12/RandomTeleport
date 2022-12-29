package com.shanebeestudios.rtp;

import com.shanebeestudios.rtp.command.RTPCommand;
import com.shanebeestudios.rtp.config.Config;
import com.shanebeestudios.rtp.util.UpdateChecker;
import com.shanebeestudios.rtp.util.Utils;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class RandomTeleport extends JavaPlugin {

    private static RandomTeleport pluginInstance;
    private Config config;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        pluginInstance = this;
        Utils.log("Loading...");

        this.config = new Config(this);
        if (this.config.checkForUpdates()) {
            new UpdateChecker(this);
        }

        registerCommands();

        float finish = (float) (System.currentTimeMillis() - start) / 1000;
        Utils.log("&aSuccessfully enabled v%s &7in &b%.2f seconds", getDescription().getVersion(), finish);
    }

    @SuppressWarnings("ConstantConditions")
    private void registerCommands() {
        getCommand("rtp").setExecutor(new RTPCommand(this));
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

}
