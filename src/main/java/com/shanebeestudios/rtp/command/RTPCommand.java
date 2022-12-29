package com.shanebeestudios.rtp.command;

import com.shanebeestudios.rtp.RandomTeleport;
import com.shanebeestudios.rtp.teleport.RandomTeleporter;
import com.shanebeestudios.rtp.util.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RTPCommand implements TabExecutor {

    private final RandomTeleporter teleporter;

    public RTPCommand(RandomTeleport plugin) {
        this.teleporter = new RandomTeleporter(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            teleporter.rtp(player);
        } else {
            Utils.sendMsg(sender, "&cThis is a player only command");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }

}
