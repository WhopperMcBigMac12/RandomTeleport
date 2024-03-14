package com.shanebeestudios.rtp.teleport;

import com.google.common.collect.ImmutableSet;
import com.shanebeestudios.rtp.RandomTeleport;
import com.shanebeestudios.rtp.config.Config;
import com.shanebeestudios.rtp.util.Utils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class RandomTeleporter {

    private final RandomTeleport plugin;
    private final Config config;
    private final BukkitScheduler bukkitScheduler;
    private final List<String> teleportingPlayers = new ArrayList<>();

    public RandomTeleporter(RandomTeleport plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
        this.bukkitScheduler = Bukkit.getScheduler();
        ((Logger) LogManager.getRootLogger()).addFilter(new MessageFilter());
    }

    public void rtp(Player player, World world) {
        Utils.sendMsg(player, "Looking for a suitable location...");
        getSafeLocation(world).thenApply(location -> {
            if (location != null) {
                Utils.sendMsg(player, "&aFound a suitable location!");
                String playerName = player.getName();
                if (!this.teleportingPlayers.contains(playerName)) {
                    this.teleportingPlayers.add(playerName);
                }
                player.teleportAsync(location).thenAccept(a ->
                    bukkitScheduler.runTaskLater(this.plugin,
                        () -> teleportingPlayers.remove(playerName), 5));
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
        int max = getMaxY(world);
        Location loc;
        if (world.getEnvironment() == Environment.NETHER) {
            loc = location.clone();
            loc.setY(world.getMinHeight());
        } else {
            loc = world.getHighestBlockAt(location, HeightMap.MOTION_BLOCKING_NO_LEAVES).getLocation();
        }

        while (loc.getY() < max) {
            loc.add(0, 1, 0);
            if (isSafe(loc)) {
                return loc;
            }
        }
        return null;
    }

    /**
     * Blocks the player should not spawn on/in
     */
    private final ImmutableSet<Material> OUCHIE_BLOCKS = ImmutableSet.<Material>builder()
        .add(Material.CACTUS)
        .add(Material.CAMPFIRE)
        .add(Material.FIRE)
        .add(Material.LAVA)
        .add(Material.MAGMA_BLOCK)
        .add(Material.POINTED_DRIPSTONE)
        .add(Material.POWDER_SNOW)
        .add(Material.SOUL_CAMPFIRE)
        .add(Material.SOUL_FIRE)
        .add(Material.SWEET_BERRY_BUSH)
        .add(Material.WATER)
        .build();

    @SuppressWarnings("RedundantIfStatement")
    private boolean isSafe(Location location) {
        Block at = location.getBlock();
        Block up = at.getRelative(BlockFace.UP);
        Block down = at.getRelative(BlockFace.DOWN);
        if (!at.isSolid() && !up.isSolid() && down.isSolid()) {
            Material downType = down.getType();
            Material atType = at.getType();
            if (Tag.PRESSURE_PLATES.isTagged(downType)) return false;
            if (OUCHIE_BLOCKS.contains(downType)) return false;
            if (OUCHIE_BLOCKS.contains(atType)) return false;

            return true;
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

        return switch (world.getEnvironment()) {
            case NORMAL,THE_END -> 200; // We don't need to test too high
            case NETHER -> 127;
            default -> world.getMaxHeight() - 1;
        };
    }

    private class MessageFilter extends AbstractFilter {

        @Override
        public Filter.Result filter(LogEvent event) {
            return event == null ? Filter.Result.NEUTRAL : checkMessage(event.getMessage().getFormattedMessage());
        }

        @Override
        public Filter.Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
            return checkMessage(msg.getFormattedMessage());
        }

        @Override
        public Filter.Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
            return checkMessage(msg.toString());
        }

        @Override
        public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
            return checkMessage(msg);
        }

        private Filter.Result checkMessage(String message) {
            if (message.contains("moved too quickly")) {
                for (String teleportingPlayer : teleportingPlayers) {
                    if (message.contains(teleportingPlayer)) {
                        return Filter.Result.DENY;
                    }
                }
            }
            return Filter.Result.NEUTRAL;
        }
    }

}
