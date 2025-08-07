package com.shanebeestudios.rtp.command;

import com.shanebeestudios.rtp.RandomTeleport;
import com.shanebeestudios.rtp.permission.Permissions;
import com.shanebeestudios.rtp.teleport.RandomTeleporter;
import com.shanebeestudios.rtp.util.Utils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.WorldArgument;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class RTPCommand {

    public RTPCommand(RandomTeleport plugin) {
        registerCommand(new RandomTeleporter(plugin));
    }

    private void registerCommand(RandomTeleporter teleporter) {
        new CommandTree("randomteleport")
            .executesPlayer(info -> {
                Player sender = info.sender();
                teleporter.rtp(sender, sender.getWorld());
            })
            .then(new WorldArgument("world")
                .withPermission(Permissions.COMMAND_RTP_WORLD.permission())
                .executesPlayer(info -> {
                    Player sender = info.sender();
                    World world = (World) info.args().get("world");
                    teleporter.rtp(sender, world);
                }))
            .then(new EntitySelectorArgument.ManyPlayers("players")
                .withPermission(Permissions.COMMAND_RTP_OTHER.permission())
                .then(new WorldArgument("world")
                    .setOptional(true)
                    .executes((sender, args) -> {

                        @SuppressWarnings("unchecked")
                        Collection<Player> players = (Collection<Player>) args.get("players");
                        assert players != null;
                        players.forEach(player -> {
                            World world = (World) args.getOrDefault("world", player.getWorld());
                            if (player != sender) sendOther(player, sender, world);
                            teleporter.rtp(player, world);
                        });
                    })))

            .register();
    }

    private void sendOther(Player player, CommandSender sender, @NotNull World world) {
        Utils.sendMsg(sender, "Teleporting &b%s &7to a random location in &b%s&7.", player.getName(), world.getName());
        Utils.sendMsg(player, "&b%s &7is teleporting you to a random location in &b%s&7.", sender.getName(), world.getName());
    }

}
