package com.shanebeestudios.rtp.teleport;

import com.shanebeestudios.rtp.RandomTeleport;
import com.shanebeestudios.rtp.config.Config;
import com.shanebeestudios.rtp.util.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class RandomTeleporter {

    private final Config config;

    public RandomTeleporter(RandomTeleport plugin) {
        this.config = plugin.getPluginConfig();
    }

    public void rtp(Player player) {
        World world = player.getWorld();

        Utils.sendMsg(player, "Looking for a suitable location...");
        getSafeLocation(world).thenApply(location -> {
            if (location != null) {
                Utils.sendMsg(player, "&aFound a suitable location!");
                player.teleportAsync(location);
            } else {
                Utils.sendMsg(player, "&cCouldn't find a suitable location!");
            }
            return null;
        });
    }

    public CompletableFuture<Location> getSafeLocation(World world) {
        CompletableFuture<Location> future = getRandomLocation(world);
        int maxRetries = this.config.getMaxRetries();
        for (int i = 0; i < maxRetries; i++) {
            future = future.thenApply(CompletableFuture::completedFuture)
                    .exceptionally(t -> getRandomLocation(world))
                    .thenCompose(Function.identity());
        }
        return future;
    }

    private CompletableFuture<Location> getRandomLocation(World world) {
        CompletableFuture<Location> future = new CompletableFuture<>();
        Random random = new Random();

        int maxDistance = getMaxDistance(world);
        int x = random.nextInt(maxDistance * 2) - maxDistance;
        int z = random.nextInt(maxDistance * 2) - maxDistance;
        Location location = new Location(world, x, 1, z);

        world.getChunkAtAsync(location).thenAccept(c -> {
            Location safeLocation = getHighestSafeLocation(world, location);
            if (safeLocation == null) {
                // cancel and retry
                future.cancel(true);
                return;
            }
            safeLocation.add(0.5, 0, 0.5); // center player
            future.complete(safeLocation);
        });
        return future;
    }

    @Nullable
    private Location getHighestSafeLocation(World world, Location location) {
        int min = getMinY(world);
        int max = getMaxY(world);
        Location loc = location.clone();

        loc.setY(max);
        while (loc.getY() > min) {
            loc.add(0, -1, 0);
            if (isSafe(loc)) {
                return loc;
            }
        }
        return null;
    }

    private boolean isSafe(Location location) {
        Block at = location.getBlock();
        Block up = at.getRelative(BlockFace.UP);
        Block down = at.getRelative(BlockFace.DOWN);
        if (!at.isSolid() && !up.isSolid() && down.isSolid()) {
            Material downType = down.getType();
            Material atType = at.getType();
            if (Tag.LEAVES.isTagged(downType)) {
                return false;
            }
            return downType != Material.WATER && downType != Material.LAVA && atType != Material.WATER && atType != Material.LAVA;
        }
        return false;
    }

    private int getMaxDistance(World world) {
        Map<String, Integer> maxDistances = this.config.getMaxDistances();
        String worldName = world.getName();
        if (maxDistances.containsKey(worldName)) {
            return maxDistances.get(worldName);
        }
        return 50000;
    }

    private int getMaxY(World world) {
        Map<String, Integer> maxY = this.config.getMaxY();
        String worldName = world.getName();
        if (maxY.containsKey(worldName)) return maxY.get(worldName);

        Environment environment = world.getEnvironment();
        if (environment == Environment.NORMAL) return 200;
        if (environment == Environment.NETHER) return 127;
        if (environment == Environment.THE_END) return 200;
        return world.getMaxHeight() - 1;
    }

    private int getMinY(World world) {
        Map<String, Integer> minY = this.config.getMinY();
        String worldName = world.getName();
        if (minY.containsKey(worldName)) return minY.get(worldName);

        Environment environment = world.getEnvironment();
        if (environment == Environment.NORMAL) return 61;
        return world.getMinHeight() + 1;
    }

}
