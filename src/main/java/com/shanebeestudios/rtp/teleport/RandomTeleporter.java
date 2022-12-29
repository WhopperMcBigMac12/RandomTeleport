package com.shanebeestudios.rtp.teleport;

import com.shanebeestudios.rtp.util.Utils;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
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

        CompletableFuture<Location> safeLocation = getSafeLocation(world);
        safeLocation.whenComplete((location, throwable) -> {
            if (location != null) {
                player.teleportAsync(location);
            } else {
                Utils.broadcast("Couldn't find a suitable location");
            }
        });
    }

    private CompletableFuture<Location> getRandomLocation(World world) {
        CompletableFuture<Location> future = new CompletableFuture<>();
        Random random = new Random();

        int x = random.nextInt(100000) - 50000;
        int z = random.nextInt(100000) - 50000;
        Location location = new Location(world, x, 1, z);

        world.getChunkAtAsync(location).thenAccept(c -> {
            Location innerLocation = location;
            innerLocation = world.getHighestBlockAt(innerLocation, HeightMap.MOTION_BLOCKING).getLocation();
            innerLocation.add(0.5, 1, 0.5);
            if (isSafe(innerLocation)) {
                if (isOnLeaves(innerLocation)) {
                    innerLocation = findGround(innerLocation);
                    if (innerLocation == null) {
                        future.cancel(true);
                        return;
                    }
                }
                Utils.broadcast("Found location...%s", innerLocation.getBlock().getRelative(BlockFace.DOWN).getType());
                future.complete(innerLocation);
            } else {
                Utils.broadcast("Retry...");
                future.cancel(true);
            }
        });
        return future;
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

    private boolean isSafe(Location location) {
        Material downType = location.getBlock().getRelative(BlockFace.DOWN).getType();
        return downType != Material.WATER && downType != Material.LAVA;
    }

    private boolean isOnLeaves(Location location) {
        Material downType = location.getBlock().getRelative(BlockFace.DOWN).getType();
        return Tag.LEAVES.isTagged(downType);
    }

    @Nullable
    private Location findGround(Location location) {
        while (location.getY() > 61) {
            location.add(0, -1, 0);
            Block block = location.getBlock();
            if (!block.getType().isSolid() && !block.getRelative(BlockFace.UP).isSolid() && block.getRelative(BlockFace.DOWN).isSolid()) {
                return location;
            }
        }
        return null;
    }

}
