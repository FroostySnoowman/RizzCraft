package rizzcraft.net.rizzcraft.Death;

import rizzcraft.net.rizzcraft.Main;
import rizzcraft.net.rizzcraft.BukkitSerialization;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DeathLogger {
    public final Main main;
    private final Gson gson;

    public DeathLogger(Main main) {
        this.main = main;
        this.gson = new Gson();
        this.main.getServer().getPluginManager().registerEvents(new EventListener(this), this.main);
    }

    public void logDeath(PlayerDeathEvent event) {
        try {
            try (Connection con = this.main.sql.connectionPool.getConnection()) {
                PreparedStatement pstmt = con.prepareStatement("INSERT into players_deaths (PlayerUUID,Inventory,Armor,Location) VALUES (?,?,?,?)");
                pstmt.setString(1, event.getEntity().getUniqueId().toString());
                pstmt.setString(2, BukkitSerialization.itemStackArrayToBase64(event.getEntity().getInventory().getContents()));
                pstmt.setString(3, BukkitSerialization.itemStackArrayToBase64(event.getEntity().getInventory().getArmorContents()));
                pstmt.setString(4, this.gson.toJson(event.getEntity().getLocation().serialize()));
                pstmt.executeUpdate();
                pstmt.close();
                con.close();
            }
        } catch (SQLException var7) {
            throw new ArithmeticException("\u001B[31mlogDeath() failed to log death of player: " + var7.getMessage() + "\u001B[0m");
        }
    }

    public void getDeathsByPlayer(Player player, OfflinePlayer target) {
        try {
            try (Connection con = this.main.sql.connectionPool.getConnection()) {
                PreparedStatement pstmt = con.prepareStatement(
                        "(SELECT * FROM players_deaths WHERE playerUUID=? ORDER BY Timestamp DESC LIMIT 10) ORDER BY Timestamp ASC"
                );
                pstmt.setString(1, target.getUniqueId().toString());
                ResultSet rs = pstmt.executeQuery();
                player.sendMessage(ChatColor.GREEN + "=== Showing 10 recent deaths for " + target.getName() + " ===");

                while(rs.next()) {
                    try {
                        int id = rs.getInt("ID");
                        String ts = rs.getString("Timestamp");
                        Location loc = Location.deserialize((Map)this.gson.fromJson(rs.getString("Location"), (new TypeToken<Map<String, Object>>() {
                        }).getType()));
                        ItemStack[] inv = this.removeAir(BukkitSerialization.itemStackArrayFromBase64(rs.getString("Inventory")));
                        player.sendMessage(
                                ChatColor.RESET
                                        + "#"
                                        + ChatColor.GREEN
                                        + id
                                        + ChatColor.YELLOW
                                        + " "
                                        + ts
                                        + ChatColor.RESET
                                        + " Inv Size: "
                                        + ChatColor.YELLOW
                                        + inv.length
                                        + ChatColor.RESET
                                        + " @ "
                                        + ChatColor.YELLOW
                                        + this.main.utility.locationAsString(loc)
                        );
                    } catch (IOException var11) {
                        throw new ArithmeticException("\u001B[31mgetDeathsByPlayer() IOException: " + var11.getMessage() + "\u001B[0m");
                    }
                }

                rs.close();
                pstmt.close();
                con.close();
                player.sendMessage("Use " + ChatColor.GREEN + "/rc death load " + ChatColor.AQUA + "<id>" + ChatColor.RESET + " to load the inventory");
            }
        } catch (SQLException var13) {
            var13.printStackTrace();
            throw new ArithmeticException("\u001B[31mgetDeathsByPlayer() failed to lookup player: " + var13.getMessage() + "\u001B[0m");
        }
    }

    public void generateInventoryFromDeath(Player player, String ID) {
        int id = Integer.parseInt(ID);

        try {
            try (Connection con = this.main.sql.connectionPool.getConnection()) {
                PreparedStatement pstmt = con.prepareStatement("SELECT * from players_deaths WHERE ID=?");
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    try {
                        ItemStack[] items = BukkitSerialization.itemStackArrayFromBase64(rs.getString("Inventory"));
                        OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("PlayerUUID")));
                        Inventory inv = Bukkit.createInventory(null, 54, target.getName() + "'s Death Inventory");
                        inv.setContents(items);
                        player.openInventory(inv);
                    } catch (IOException var11) {
                        throw new ArithmeticException("\u001B[31mgenerateInventoryFromDeath() failed to serialize: " + var11.getMessage() + "\u001B[0m");
                    }
                }

                pstmt.close();
                rs.close();
                con.close();
            }
        } catch (SQLException var13) {
            throw new ArithmeticException("\u001B[31mgenerateInventoryFromDeath() failed to query record: " + var13.getMessage() + "\u001B[0m");
        }
    }

    public ItemStack[] removeAir(ItemStack[] items) {
        ArrayList<ItemStack> output = new ArrayList();

        for(ItemStack itm : items) {
            if (itm != null && itm.getType() != Material.AIR) {
                output.add(itm);
            }
        }

        return output.toArray(new ItemStack[0]);
    }
}