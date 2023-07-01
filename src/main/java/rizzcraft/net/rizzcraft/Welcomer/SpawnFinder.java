package rizzcraft.net.rizzcraft.Welcomer;

import rizzcraft.net.rizzcraft.Main;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpawnFinder extends BukkitRunnable {
    volatile boolean running = true;
    private final Main plugin;
    private Player player;
    private List<String> allowedBiomes = Arrays.asList("PLAINS", "FOREST", "DARK_FOREST", "TAIGA", "JUNGLE", "SAVANNA", "BIRCH_FOREST", "FLOWER_FOREST");
    private List<String> safeBlocks = Arrays.asList("GRASS_BLOCK", "DIRT_BLOCK", "SAND", "SNOW", "SNOW_BLOCK");
    private World world = (World)Bukkit.getWorlds().get(0);
    private int biome_chunk = 1;
    private Location biome_found = null;
    private int safeBlock_block = 1;
    private Location safeBlock_found = null;

    public SpawnFinder(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void run() {
        this.plugin.logger.log(Level.INFO, "Searching for safe biome...");

        for(; this.biome_found == null; ++this.biome_chunk) {
            Location loc = this.Spiral(this.biome_chunk, 4096).toLocation(this.world);
            if (!this.world.isChunkGenerated(loc.getBlockX() / 16, loc.getBlockZ() / 16)) {
                int y = this.world.getHighestBlockYAt(loc);
                Biome b = this.world.getBiome(loc.getBlockX(), y, loc.getBlockZ());
                if (this.allowedBiomes.contains(b.name().toUpperCase())) {
                    this.plugin
                            .logger
                            .log(Level.INFO, "Found a fresh chunk for " + this.player.getName() + " at " + loc.getBlockX() / 16 + ", " + loc.getBlockZ() / 16);
                    this.biome_found = loc.clone();
                }
            }
        }

        this.plugin.logger.log(Level.INFO, "Searching for safe block...");

        while(this.biome_found != null && this.safeBlock_found == null) {
            Location loc = this.Spiral(this.safeBlock_block, 2).toLocation(this.world);
            loc.add(this.biome_found);
            loc.setY((double)this.world.getHighestBlockYAt(loc));
            Block block = this.world.getBlockAt(loc);
            if (this.safeBlocks.contains(block.getType().name())) {
                this.plugin
                        .logger
                        .log(Level.INFO, "Found a safe block for " + this.player.getName() + " at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                this.safeBlock_found = loc;
                Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.player.teleport(loc.add(0.0, 1.0, 0.0)), 1L);
                Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.player.setBedSpawnLocation(loc.add(0.0, 1.0, 0.0), true), 1L);
            } else {
                this.plugin.logger.log(Level.INFO, loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + " is unsafe. checking next block...");
                ++this.safeBlock_block;
            }
        }

        this.cancel();
    }

    public Vector Spiral(int n, int amplifier) {
        float k = (float)Math.ceil((Math.sqrt((double)n) - 1.0) / 2.0);
        float t = 2.0F * k;
        float m = (t + 1.0F) * (t + 1.0F);
        float height = 0.0F;
        if ((float)n >= m - t) {
            return new Vector((k - (m - (float)n)) * (float)amplifier, height, -k * (float)amplifier);
        } else {
            m -= t;
            if ((float)n >= m - t) {
                return new Vector(-k * (float)amplifier, height, (-k + (m - (float)n)) * (float)amplifier);
            } else {
                m -= t;
                return (float)n >= m - t
                        ? new Vector((-k + (m - (float)n)) * (float)amplifier, height, k * (float)amplifier)
                        : new Vector(k * (float)amplifier, height, (k - (m - (float)n - t)) * (float)amplifier);
            }
        }
    }
}