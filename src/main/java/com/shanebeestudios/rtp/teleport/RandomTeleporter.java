package com.shanebeestudios.rtp.teleport;

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

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class RandomTeleporter {

    public void rtp(Player player) {
        World world = player.getWorld();

        getSafeLocation(world).thenApply(location -> {
            if (location != null) {
                player.teleportAsync(location);
            } else {
                Utils.sendMsg(player, "&cCouldn't find a suitable location!");
            }
            return null;
        });
    }

    public CompletableFuture<Location> getSafeLocation(World world) {
        CompletableFuture<Location> future = getRandomLocation(world);
        for (int i = 0; i < 10; i++) {
            future = future.thenApply(CompletableFuture::completedFuture)
                    .exceptionally(t -> getRandomLocation(world))
                    .thenCompose(Function.identity());
        }
        return future;
    }

    private CompletableFuture<Location> getRandomLocation(World world) {
        CompletableFuture<Location> future = new CompletableFuture<>();
        Random random = new Random();

        int x = random.nextInt(100000) - 50000;
        int z = random.nextInt(100000) - 50000;
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
        Environment environment = world.getEnvironment();
        int min = environment == Environment.NETHER ? 0 : 60;
        int max = environment == Environment.NETHER ? 127 : 200;
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

}
