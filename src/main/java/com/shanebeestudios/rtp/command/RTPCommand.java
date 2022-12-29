package com.shanebeestudios.rtp.command;

import com.google.common.collect.ImmutableList;
import com.shanebeestudios.rtp.RandomTeleport;
import com.shanebeestudios.rtp.permission.Permissions;
import com.shanebeestudios.rtp.teleport.RandomTeleporter;
import com.shanebeestudios.rtp.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RTPCommand implements TabExecutor {

    private final RandomTeleporter teleporter;
    private final List<String> worlds = new ArrayList<>();

    public RTPCommand(RandomTeleport plugin) {
        this.teleporter = new RandomTeleporter(plugin);
        Bukkit.getWorlds().forEach(world -> worlds.add(world.getName()));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            boolean hasOtherPerm = Permissions.COMMAND_RTP_OTHER.check(player);
            boolean hasWorldPerm = Permissions.COMMAND_RTP_WORLD.check(player);
            if (args.length == 0) {
                teleporter.rtp(player, player.getWorld());
            } else if (args.length == 1) {
                Player otherPlayer = Bukkit.getPlayerExact(args[0]);
                World world = Bukkit.getWorld(args[0]);
                if (!hasOtherPerm && otherPlayer != null) {
                    Utils.sendMsg(player, "&cYou do not have permission to teleport another player.");
                    return true;
                } else if (!hasWorldPerm && world != null) {
                    Utils.sendMsg(player, "&cYou do not have permission to teleport to another world.");
                    return true;
                }
                if (hasOtherPerm) {
                    if (otherPlayer != null) {
                        Utils.sendMsg(player, "Teleporting &b%s &7to a random location.", otherPlayer.getName());
                        Utils.sendMsg(otherPlayer, "&b%s &7is teleporting you to a random location.", player.getName());
                        teleporter.rtp(otherPlayer, otherPlayer.getWorld());
                    } else {
                        Utils.sendMsg(player, "&cInvalid player %s", args[0]);
                    }
                } else if (hasWorldPerm) {
                    if (world != null) {
                        teleporter.rtp(player, world);
                    } else {
                        Utils.sendMsg(player, "&cInvalid world &7%s", args[0]);
                    }
                }
            } else if (args.length == 2) {
                if (!hasWorldPerm) Utils.sendMsg(player, "&cYou do not have permission to teleport to another world.");
                else if (!hasOtherPerm)
                    Utils.sendMsg(player, "&cYou do not have permission to teleport another player.");
                else {
                    Player otherPlayer = Bukkit.getPlayerExact(args[0]);
                    World world = Bukkit.getWorld(args[1]);
                    if (otherPlayer == null) {
                        Utils.sendMsg(player, "&cInvalid player %s", args[0]);
                    } else if (world == null) {
                        Utils.sendMsg(player, "&cInvalid world &7%s", args[1]);
                    } else {
                        Utils.sendMsg(player, "Teleporting &b%s &7to a random location in &b%s&7.", otherPlayer.getName(), world.getName());
                        Utils.sendMsg(otherPlayer, "&b%s &7is teleporting you to a random location in &b%s&7.", player.getName(), world.getName());
                        teleporter.rtp(otherPlayer, world);
                    }
                }
            }
        } else {
            if (args.length == 0) {
                Utils.sendMsg(sender, "&cPlease provide at least a player to randomly teleport.");
            } else if (args.length == 1) {
                Player otherPlayer = Bukkit.getPlayerExact(args[0]);
                if (otherPlayer != null) {
                    Utils.sendMsg(sender, "Teleporting &b%s &7to a random location.", otherPlayer.getName());
                    Utils.sendMsg(otherPlayer, "&bServer &7is teleporting you to a random location.");
                    teleporter.rtp(otherPlayer, otherPlayer.getWorld());
                } else {
                    Utils.sendMsg(sender, "&cInvalid player %s", args[0]);
                }
            } else if (args.length == 2) {
                Player otherPlayer = Bukkit.getPlayerExact(args[0]);
                World world = Bukkit.getWorld(args[1]);
                if (otherPlayer == null) Utils.sendMsg(sender, "&cInvalid player %s", args[0]);
                else if (world == null) Utils.sendMsg(sender, "&cInvalid world &7%s", args[1]);
                else {
                    Utils.sendMsg(sender, "Teleporting &b%s &7to a random location.", otherPlayer.getName());
                    Utils.sendMsg(otherPlayer, "&bServer &7is teleporting you to a random location in &b%s&7.", world.getName());
                    teleporter.rtp(otherPlayer, world);
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        boolean hasOtherPerm = Permissions.COMMAND_RTP_OTHER.check(sender);
        boolean hasWorldPerm = Permissions.COMMAND_RTP_WORLD.check(sender);
        if (hasWorldPerm && !hasOtherPerm) {
            if (args.length == 1) {
                return getPartialMatches(args[0], worlds, new ArrayList<>());
            }
        } else if (!hasWorldPerm && hasOtherPerm) {
            if (args.length == 1) {
                // defaults to all player names
                return null;
            }
        } else if (hasWorldPerm) {
            if (args.length == 1) {
                // defaults to all player names
                return null;
            } else if (args.length == 2) {
                return getPartialMatches(args[1], worlds, new ArrayList<>());
            }
        }
        return ImmutableList.of();
    }

    @NotNull
    private <T extends Collection<? super String>> T getPartialMatches(@NotNull final String token, @NotNull final Iterable<String> originals, @NotNull final T collection) {
        for (String string : originals) {
            if (string.contains(token)) {
                collection.add(string);
            }
        }
        return collection;
    }

}
