package com.shanebeestudios.rtp.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.shanebeestudios.rtp.RandomTeleport;
import com.shanebeestudios.rtp.config.Config;
import com.shanebeestudios.rtp.permission.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Consumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker implements Listener {

    private String updateVersion;
    private final RandomTeleport plugin;
    private final Config config;

    public UpdateChecker(RandomTeleport plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        checkForUpdate(plugin.getDescription().getVersion());
    }

    public void checkForUpdate(String pluginVersion) {
        Utils.log("Checking for update...");
        if (pluginVersion.contains("-")) {
            Utils.log("&eYou're running a beta version, no need to check for an update!");
            return;
        }
        getVersion(version -> {
            if (version.equalsIgnoreCase(pluginVersion)) {
                Utils.log("&aPlugin is up to date!");
            } else {
                Utils.log("&cPlugin is not up to date!");
                Utils.log(" - Current version: &cv%s", pluginVersion);
                Utils.log(" - Available update: &bv%s", version);
                Utils.log(" - Download available at: &bhttps://github.com/ShaneBeee/RandomTeleport/releases");
                updateVersion = version;
            }
        });
    }

    private void getVersion(final Consumer<String> consumer) {
        try {
            URL url = new URL("https://api.github.com/repos/ShaneBeee/RandomTeleport/releases/latest");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            String tag_name = jsonObject.get("tag_name").getAsString();
            consumer.accept(tag_name);
        } catch (IOException e) {
            Utils.log("&cChecking for update failed!");
            if (this.config.isDebug()) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if (updateVersion == null) return;

        Player player = event.getPlayer();
        if (!Permissions.UPDATE_CHECK.check(player)) return;

        Bukkit.getScheduler().runTaskLater(plugin, bukkitTask -> {
            Utils.sendMsg(player, "&7update available: &a" + updateVersion);
            Utils.sendMsg(player, "&7download at &bhttps://github.com/ShaneBeee/RandomTeleport/releases");
        }, 60);
    }

}
