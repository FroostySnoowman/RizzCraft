package rizzcraft.net.rizzcraft.Welcomer;

import rizzcraft.net.rizzcraft.Main;
import java.io.FileReader;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Welcomer {
    public final Main main;
    public final ArrayList<ItemStack> selectionOptions;

    public Welcomer(Main main) {
        this.main = main;
        this.selectionOptions = new ArrayList();
        this.createSelectionItems();
        Objects.requireNonNull(this.main.getCommand("spawn")).setExecutor(new CommandManager(this));

        this.main.getServer().getPluginManager().registerEvents(new EventListener(this, this.main), this.main);
    }

    public void welcome(Player player, int section) {
        if (section == 1) {
            Inventory menu = Bukkit.createInventory(null, 9, "Select Starting Items");
            menu.setItem(3, (ItemStack)this.selectionOptions.get(0));
            menu.setItem(4, (ItemStack)this.selectionOptions.get(1));
            menu.setItem(5, (ItemStack)this.selectionOptions.get(2));
            Bukkit.getScheduler().runTaskLater(this.main, () -> player.openInventory(menu), 20L);
        }
    }

    public void check(Player player) {
        if (player != null) {
            try (Connection con = this.main.sql.connectionPool.getConnection()) {
                PreparedStatement pstmt = con.prepareStatement("SELECT Welcomed FROM players WHERE UUID=?");
                pstmt.setString(1, player.getUniqueId().toString());
                ResultSet rs = pstmt.executeQuery();
                int welcomed = 0;
                if (rs.next()) {
                    welcomed = rs.getInt("Welcomed");
                }

                pstmt.close();
                rs.close();
                con.close();
                if (welcomed == 1) {
                    List<String> welcomer = Arrays.asList(
                            "Welcome back, {player}!",
                            "Hey, {player}!",
                            "Good to see you, {player}!",
                            "How's it going, {player}?",
                            "Good to have you back, {player}!",
                            "Today is a good day, {player}!",
                            "Adventure awaits you, {player}!",
                            "Hello there!",
                            "You've got this, {player}!",
                            "Aye! It's {player}!",
                            "Welcome back, friend!",
                            "Good things await you, {player}!",
                            "Hey everyone look, it's {player}!",
                            "The party has started now you're here, {player}!",
                            "Well if it isn't the only {player}!",
                            "<3",
                            ":D",
                            "Things just got better with you here!"
                    );
                    List<String> tips = Arrays.asList(
                            "You can change your nickname color with /help nick",
                            "You can hide yourself from the map with /dynmap hide",
                            "You can create fully vanilla trading shops with /shop",
                            "You can toggle /co i to see historical block changes",
                            "Skipping the night only requires 20% of players to sleep",
                            "If a shop is within your claim, you may need to run /containertrust public to allow access!",
                            "You can protect your base from griefers with /help claim"
                    );
                    int rnd = new Random().nextInt(welcomer.size());
                    int rnd2 = new Random().nextInt(tips.size());
                    Bukkit.getScheduler()
                            .runTaskLater(this.main, () -> player.sendTitle(" ", welcomer.get(rnd).replace("{player}", player.getName()), 5, 50, 5), 60L);
                    Bukkit.getScheduler().runTaskLater(this.main, () -> player.sendMessage(ChatColor.GRAY + "Tip: " + (String)tips.get(rnd2)), 60L);
                } else {
                    try (Connection con2 = this.main.sql.connectionPool.getConnection()) {
                        PreparedStatement pstmt2 = con2.prepareStatement("UPDATE players SET Welcomed=1 WHERE UUID=?");
                        pstmt2.setString(1, player.getUniqueId().toString());
                        pstmt2.executeUpdate();
                        pstmt2.close();
                        con2.close();
                        this.main.logger.log(Level.INFO, "Updated welcome record for player " + player.getUniqueId());
                    } catch (SQLException var13) {
                        throw new ArithmeticException("[RizzCraft] Error updating welcome record for player " + player.getUniqueId() + " - " + var13.getMessage());
                    }

                    this.welcome(player, 1);
                    // Removed For Lag... Can Add Back If Needed
                    //BukkitTask var16 = new SpawnFinder(this.main, player).runTaskAsynchronously(this.main);
                }
            } catch (SQLException var15) {
                throw new ArithmeticException("[RizzCraft] Error checking if player " + player.getUniqueId() + " has been welcomed - " + var15.getMessage());
            }
        }
    }

    public void updateStats(Player player) {
        try {
            try (Connection con = this.main.sql.connectionPool.getConnection()) {
                JSONParser parser = new JSONParser();
                Object objStats = parser.parse(new FileReader(System.getProperty("user.dir") + "/world/stats/" + player.getUniqueId() + ".json"));
                Object objAdvs = parser.parse(new FileReader(System.getProperty("user.dir") + "/world/advancements/" + player.getUniqueId() + ".json"));
                JSONObject jsonStats = (JSONObject)objStats;
                JSONObject jsonAdvs = (JSONObject)objAdvs;
                parser.reset();
                PreparedStatement pstmt = con.prepareStatement("UPDATE players SET StatsJSON=?, AdvancementsJSON=? WHERE UUID=?");
                pstmt.setString(1, jsonStats.toJSONString());
                pstmt.setString(2, jsonAdvs.toJSONString());
                pstmt.setString(3, player.getUniqueId().toString());
                pstmt.executeUpdate();
                pstmt.close();
                con.close();
            }
        } catch (Exception var11) {
            throw new ArithmeticException("[RizzCraft] Error updating stats for player " + player.getUniqueId() + " - " + var11.getMessage());
        }
    }

    public void updateName(Player player) {
        try {
            try (Connection con = this.main.sql.connectionPool.getConnection()) {
                PreparedStatement pstmt = con.prepareStatement("UPDATE players SET DisplayName=? WHERE UUID=?");
                pstmt.setString(1, player.getDisplayName());
                pstmt.setString(2, player.getUniqueId().toString());
                pstmt.executeUpdate();
                pstmt.close();
                con.close();
            }
        } catch (SQLException var7) {
            throw new ArithmeticException("[RizzCraft] Error updating name record for player " + player.getUniqueId() + " - " + var7.getMessage());
        }
    }

    public void createSelectionItems() {
        ItemStack nothing = new ItemStack(Material.BARRIER);
        ItemMeta nothing_im = nothing.getItemMeta();

        assert nothing_im != null;

        nothing_im.setDisplayName(ChatColor.RED + "Nothing! I'm hardcore!");
        nothing_im.setLore(List.of("1x Air"));
        nothing.setItemMeta(nothing_im);
        this.selectionOptions.add(nothing);
        ItemStack minimal = new ItemStack(Material.RED_BED);
        ItemMeta minimal_im = minimal.getItemMeta();

        assert minimal_im != null;

        minimal_im.setDisplayName(ChatColor.RED + "A bed & some food");
        minimal_im.setLore(Arrays.asList("16x Cooked Beef", "1x Red Bed"));
        minimal.setItemMeta(minimal_im);
        this.selectionOptions.add(minimal);
        ItemStack tools = new ItemStack(Material.STONE_PICKAXE);
        ItemMeta tools_im = tools.getItemMeta();

        assert tools_im != null;

        tools_im.setDisplayName(ChatColor.RED + "A bed, food & some tools");
        tools_im.setLore(Arrays.asList("1x Stone Axe", "1x Stone Pickaxe", "16x Cooked Beef", "1x Red Bed"));
        tools.setItemMeta(tools_im);
        this.selectionOptions.add(tools);
    }

    public boolean looksLikeALT(Player player) {
        InetAddress ipAddr = Objects.requireNonNull(player.getAddress()).getAddress();

        for(Player p : Bukkit.getOnlinePlayers()) {
            if (Objects.requireNonNull(p.getAddress()).getAddress().equals(ipAddr) && !p.equals(player)) {
                this.main.utility.alertMods(this.main.prefix + ChatColor.GRAY + player.getName() + " looks like an alt of " + p.getName());
                return true;
            }
        }

        return false;
    }
}