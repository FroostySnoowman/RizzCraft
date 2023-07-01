package rizzcraft.net.rizzcraft;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Utility {
    private final Main main;

    public Utility(Main main) {
        this.main = main;
    }

    public UUID getPlayerUUID(String name) {
        if (name == null) {
            throw new IllegalArgumentException("\u001B[31mName cannot be null when getting player UUID.\u001B[0m");
        } else {
            OfflinePlayer target = Bukkit.getPlayerExact(name);
            if (target != null) {
                return target.getUniqueId();
            } else {
                OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();

                for(OfflinePlayer offlinePlayer : offlinePlayers) {
                    if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(name)) {
                        return offlinePlayer.getUniqueId();
                    }
                }

                return null;
            }
        }
    }

    public ArrayList<Block> getBlocksInRegion(Location A, Location B, Material... OptionalFilter) {
        ArrayList<Block> output = new ArrayList();
        Material Filter = OptionalFilter.length >= 1 ? OptionalFilter[0] : null;
        int max_x;
        int min_x;
        if (A.getBlockX() > B.getBlockX()) {
            max_x = A.getBlockX();
            min_x = B.getBlockX();
        } else {
            max_x = B.getBlockX();
            min_x = A.getBlockX();
        }

        int max_y;
        int min_y;
        if (A.getBlockY() > B.getBlockY()) {
            max_y = A.getBlockY();
            min_y = B.getBlockY();
        } else {
            max_y = B.getBlockY();
            min_y = A.getBlockY();
        }

        int max_z;
        int min_z;
        if (A.getBlockZ() > B.getBlockZ()) {
            max_z = A.getBlockZ();
            min_z = B.getBlockZ();
        } else {
            max_z = B.getBlockZ();
            min_z = A.getBlockZ();
        }

        for(int x = min_x; x < max_x + 1; ++x) {
            for(int y = min_y; y < max_y + 1; ++y) {
                for(int z = min_z; z < max_z + 1; ++z) {
                    Block block_ = new Location(A.getWorld(), (double)x, (double)y, (double)z).getBlock();
                    if (Filter != null && Filter == block_.getType()) {
                        output.add(block_);
                    }

                    if (Filter == null) {
                        output.add(block_);
                    }
                }
            }
        }

        return output;
    }

    public boolean isBlockInRegion(Location A, Location B, Block block) {
        int max_x;
        int min_x;
        if (A.getBlockX() > B.getBlockX()) {
            max_x = A.getBlockX();
            min_x = B.getBlockX();
        } else {
            max_x = B.getBlockX();
            min_x = A.getBlockX();
        }

        int max_y;
        int min_y;
        if (A.getBlockY() > B.getBlockY()) {
            max_y = A.getBlockY();
            min_y = B.getBlockY();
        } else {
            max_y = B.getBlockY();
            min_y = A.getBlockY();
        }

        int max_z;
        int min_z;
        if (A.getBlockZ() > B.getBlockZ()) {
            max_z = A.getBlockZ();
            min_z = B.getBlockZ();
        } else {
            max_z = B.getBlockZ();
            min_z = A.getBlockZ();
        }

        for(int x = min_x; x < max_x + 1; ++x) {
            for(int y = min_y; y < max_y + 1; ++y) {
                for(int z = min_z; z < max_z + 1; ++z) {
                    Block block_ = new Location(A.getWorld(), (double)x, (double)y, (double)z).getBlock();
                    if (block_.equals(block)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public Player getRandomOnlinePlayer() {
        int player_n = new Random().nextInt(Bukkit.getOnlinePlayers().size());
        return (Player)Bukkit.getOnlinePlayers().toArray()[player_n];
    }

    public ArrayList<Block> getBlocksInRadiusOf(Location location, int radius, Material... OptionalFilter) {
        ArrayList<Block> output = new ArrayList();
        Material Filter = OptionalFilter.length >= 1 ? OptionalFilter[0] : null;
        int max_x = location.getBlockX() + radius;
        int max_y = location.getBlockY() + radius;
        int max_z = location.getBlockZ() + radius;
        int min_x = location.getBlockX() - radius;
        int min_y = location.getBlockY() - radius;
        int min_z = location.getBlockZ() - radius;

        for(int x = min_x; x < max_x + 1; ++x) {
            for(int y = min_y; y < max_y + 1; ++y) {
                for(int z = min_z; z < max_z + 1; ++z) {
                    Block block_ = new Location(location.getWorld(), (double)x, (double)y, (double)z).getBlock();
                    if (Filter != null && Filter == block_.getType()) {
                        output.add(block_);
                    }

                    if (Filter == null) {
                        output.add(block_);
                    }
                }
            }
        }

        return output;
    }

    public String locationAsString(Location location) {
        return "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";
    }

    public void alertMods(String message) {
        for(Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("rc.mod")) {
                p.sendMessage(message);
            }
        }
    }

    public Object getPlayer(Player requestingPlayer, String name) {
        UUID targetID = this.getPlayerUUID(name.toLowerCase());
        if (targetID == null) {
            if (requestingPlayer != null) {
                requestingPlayer.sendMessage(this.main.prefix + "That player doesn't exist.");
            }

            return null;
        } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetID);
            return target.isOnline() ? Bukkit.getPlayer(targetID) : target;
        }
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

    public void TeleportPlayerInXSeconds(Player player, int seconds, int x, int y, int z) {
        Location currentPosition = player.getLocation();
        player.sendMessage(this.main.prefix + "Teleporting in 3s... Stay still.");

        Bukkit.getScheduler().runTaskLater(this.main, () -> {

            if (
                    player.getLocation().getBlockX() == currentPosition.getBlockX() &&
                            player.getLocation().getBlockY() == currentPosition.getBlockY() &&
                            player.getLocation().getBlockZ() == currentPosition.getBlockZ()
            )
            {
                Location newLocation = new Location(Bukkit.getWorld("world"), x + 0.5, y, z + 0.5);
                player.teleport(newLocation);
            } else
            {
                player.sendMessage(this.main.prefix + "Teleport cancelled. You moved!");
            }

        }, 60);

    }
}