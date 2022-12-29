package com.shanebeestudios.rtp;

import com.shanebeestudios.rtp.command.RTPCommand;
import com.shanebeestudios.rtp.util.Utils;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomTeleport extends JavaPlugin {

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onEnable() {
        Utils.log("Loading...");
        getCommand("rtp").setExecutor(new RTPCommand());
        Utils.log("Successfully loaded!!!");
    }

}
