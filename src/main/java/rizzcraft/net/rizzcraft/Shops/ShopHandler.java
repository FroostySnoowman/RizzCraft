package rizzcraft.net.rizzcraft.Shops;

import rizzcraft.net.rizzcraft.Main;
import rizzcraft.net.rizzcraft.BukkitSerialization;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;


public class ShopHandler {

    public final Main main;
    public final HashMap<Player, String[]> PlayersInteracting;
    public final HashMap<Shop, HashMap<Player, Integer>> BuyingQuantities;
    public boolean lockedShops = false;
    public boolean lockedChests = false;
    private final Gson gson;

    public ShopHandler(Main main) {
        this.main = main;
        this.PlayersInteracting = new HashMap<Player, String[]>();
        this.BuyingQuantities = new HashMap<Shop, HashMap<Player, Integer>>();
        this.gson = new Gson();

        Objects.requireNonNull(this.main.getCommand("item")).setExecutor(new CommandManager(this));
        Objects.requireNonNull(this.main.getCommand("shop")).setExecutor(new CommandManager(this));
        Objects.requireNonNull(this.main.getCommand("shop")).setTabCompleter(null);


        this.main.getServer().getPluginManager().registerEvents(new EventListener(this), this.main);
    }

    public void activatePointer(Player player, String[] state, String message) {
        this.PlayersInteracting.put(player, state);
        player.sendMessage(message);
    }

    public Shop createShop(Player owner, Location location) {
        if (!(this.isValidInstance(location.getBlock().getState()))) {
            owner.sendMessage(this.main.prefix + ChatColor.RED + "Shop cannot be of type " + location.getBlock().getType());
            return null;
        }

        PreparedStatement pstmt0;
        try (Connection con = this.main.sql.connectionPool.getConnection()) {
            pstmt0 = con.prepareStatement("SELECT ID FROM shops WHERE Location=?");
            pstmt0.setString(1, location.toString());
            ResultSet rs = pstmt0.executeQuery();
            if (rs.next()) {
                owner.sendMessage(this.main.prefix + ChatColor.RED + "A shop already exists here");
                rs.close();
                pstmt0.close();
                return null;
            }
            rs.close();
            pstmt0.close();
            con.close();
        } catch (SQLException e1) {
            throw new ArithmeticException("[RizzCraft] Failed to lookup shop at location " + this.main.utility.locationAsString(location) + ": " + e1.getMessage());
        }

        try (Connection con = this.main.sql.connectionPool.getConnection()) {
            PreparedStatement pstmt = con.prepareStatement("INSERT INTO shops(OwnerUUID,Stock,Payment,PaymentCount,Location) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, owner.getUniqueId().toString());
            pstmt.setString(2, "");
            pstmt.setString(3, BukkitSerialization.itemStackToBase64(owner.getInventory().getItemInMainHand()));
            pstmt.setInt(4, 0);
            pstmt.setString(5, this.gson.toJson(location.serialize()));
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (!(rs.next()))
                throw new ArithmeticException("No result");

            int id = rs.getInt(1);
            pstmt.close();
            con.close();
            this.main.logger.log(Level.INFO, "New shop (" + id + ") created by " + owner.getUniqueId() + " at location " + this.main.utility.locationAsString(location));
            owner.sendMessage(this.main.prefix + ChatColor.GREEN + "Shop created! Items will cost " + owner.getInventory().getItemInMainHand().getAmount() + "x" + owner.getInventory().getItemInMainHand().getType());
            return new Shop(id, owner.getUniqueId(), null, null, owner.getInventory().getItemInMainHand(), 0, location.getBlock().getLocation(), false, 1);
        } catch (SQLException e) {
            owner.sendMessage(this.main.prefix + ChatColor.RED + "Failed to create the new shop :(");
            throw new ArithmeticException("[RizzCraft] Failed to create shop " + owner.getUniqueId() + " at " + this.main.utility.locationAsString(location) + ": " + e.getMessage());
        }
    }

    public String stockAsString(Shop shop) {
        ArrayList<Material> output = new ArrayList<Material>();

        if (shop.Stock != null && shop.Stock.length > 0) {
            for (ItemStack item : shop.Stock) {
                if (item != null && !(output.contains(item.getType()))) {
                    output.add(item.getType());
                }
            }
        }

        return Arrays.toString(output.toArray());
    }

    @SuppressWarnings("SameReturnValue")
    public void saveShop(Shop shop) {
        try (Connection con = this.main.sql.connectionPool.getConnection()) {
            PreparedStatement pstmt = con.prepareStatement("UPDATE shops SET OwnerUUID=?, shopName=?, SellsItems=?, Stock=?, Payment=?, PaymentCount=?, Location=?, Locked=?, Quantity=? WHERE OwnerUUID=? AND Location=?");
            pstmt.setString(1, shop.OwnerUID.toString());
            pstmt.setString(2, shop.shopName);
            pstmt.setString(3, this.stockAsString(shop));
            pstmt.setString(4, BukkitSerialization.itemStackArrayToBase64(shop.Stock));
            pstmt.setString(5, BukkitSerialization.itemStackToBase64(shop.Payment));
            pstmt.setInt(6, shop.PaymentCount);
            pstmt.setString(7, this.gson.toJson(shop.Location.serialize()));
            pstmt.setInt(8, shop.Locked ? 1 : 0);
            pstmt.setInt(9, shop.Quantity);
            pstmt.setString(10, shop.OwnerUID.toString());
            pstmt.setString(11, this.gson.toJson(shop.Location.serialize()));
            int rows = pstmt.executeUpdate();
            pstmt.close();
            con.close();

            if (rows > 0) {
                this.main.logger.log(Level.INFO, "Shop ID " + shop.ID + " has been saved");
                return;
            }
            throw new SQLException("Failed to update Shop ID " + shop.ID);

        } catch (SQLException e) {
            throw new ArithmeticException("[RizzCraft: saveShop() failed to save for shop ID " + shop.ID + " : " + e.getMessage());
        }
    }

    public Shop getShop(Location location) {
        try (Connection con = this.main.sql.connectionPool.getConnection()) {
            Shop shop = null;
            PreparedStatement pstmt = con.prepareStatement("SELECT * from shops WHERE Location=?");
            pstmt.setString(1, this.gson.toJson(location.serialize()));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
                shop = this.shopFromSQL(rs);

            rs.close();
            pstmt.close();
            con.close();
            return shop;

        } catch (SQLException e) {
            this.lockedChests = true;
            this.lockedShops = true;
            this.main.utility.alertMods(this.main.prefix + "Chests have been locked across the server because of an error retrieving shop data...");
            throw new ArithmeticException("[RizzCraft] Failed to getShop() from location " + this.main.utility.locationAsString(location) + ": " + e.getMessage());
        }
    }

    public Shop shopFromSQL(ResultSet rs) {
        try {
            int ID = rs.getInt("ID");
            UUID uuid = UUID.fromString(rs.getString("OwnerUUID"));
            int PaymentCount = rs.getInt("PaymentCount");
            int Quantity = rs.getInt("Quantity");
            Location ShopLocation = Location.deserialize(this.gson.fromJson(rs.getString("Location"), new TypeToken<Map<String, Object>>(){}.getType()));

            String shopName = rs.getString("shopName");

            ItemStack Payment = null;
            try {
                if (!(rs.getString("Payment").equals(""))) {
                    Payment = BukkitSerialization.itemStackFromBase64(rs.getString("Payment"));
                }
            } catch (IOException e1) {
                throw new ArithmeticException("[RizzCraft] Failed to serialize Payment ItemStack");
            }

            ItemStack[] Stock = null;
            try {
                if (rs.getString("Stock") != null && !rs.getString("Stock").equals("")) {
                    Stock = BukkitSerialization.itemStackArrayFromBase64(rs.getString("Stock"));
                }
            } catch (IOException e) {
                throw new ArithmeticException("[RizzCraft] Failed to serialize Stock ItemStack Array");
            }

            boolean Locked = false;
            if (rs.getInt("Locked") == 1) {
                Locked = true;
            }

            this.main.logger.log(Level.INFO, "shopFomSQL() serialized ID " + ID + " to a Shop");
            return new Shop(ID, uuid, shopName, Stock, Payment, PaymentCount, ShopLocation, Locked, Quantity);

        } catch (SQLException e) {
            throw new ArithmeticException("[RizzCraft] shopFromSQL() failed to complete: " + e.getMessage());
        }
    }

    public ArrayList<Shop> shopsFromSQL(ResultSet rs) {
        this.main.logger.log(Level.INFO, "shopsFromSQL() called");
        ArrayList<Shop> shops = new ArrayList<Shop>();

        try {
            while (rs.next()) {
                shops.add(this.shopFromSQL(rs));
            }
            return shops;
        } catch (SQLException e) {
            throw new ArithmeticException("[RizzCraft] shopsFromSQL() failed to create shop: " + e.getMessage());
        }
    }

    @SuppressWarnings("SameReturnValue")
    public boolean deleteShop(int ID) {
        try (Connection con = this.main.sql.connectionPool.getConnection()) {

            PreparedStatement pstmt = con.prepareStatement("DELETE FROM shops_transactions WHERE ShopID=?");
            pstmt.setInt(1, ID);
            pstmt.executeUpdate();

            PreparedStatement pstmt2 = con.prepareStatement("DELETE from shops WHERE ID=?");
            pstmt2.setInt(1, ID);
            pstmt2.executeUpdate();

            pstmt.close();
            pstmt2.close();
            con.close();

            this.main.logger.log(Level.INFO, "Shop ID: " + ID + " was deleted");
            return true;

        } catch (SQLException e1) {
            throw new ArithmeticException("[RizzCraft] Failed to delete shop ID: " + ID + " - " + e1.getMessage());
        }
    }

    public void transferOwnership(Shop shop, Player currentOwner, OfflinePlayer newOwner) {
        if (shop == null) {
            currentOwner.sendMessage(this.main.prefix + ChatColor.RED + "That container is not a shop.");
            return;
        }
        shop.OwnerUID = newOwner.getUniqueId();
        this.saveShop(shop);
        currentOwner.sendMessage(this.main.prefix + ChatColor.GREEN + "Shop has been successfully transferred to " + newOwner.getName());
        if (newOwner.isOnline()) {
            Objects.requireNonNull(newOwner.getPlayer()).sendMessage(this.main.prefix + ChatColor.GREEN + currentOwner.getName() + " has transferred you their shop located at " + this.main.utility.locationAsString(shop.Location));
        }
    }

    public void findShopsSelling(Material material, Player player) {
        try (Connection con = this.main.sql.connectionPool.getConnection()) {
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM shops WHERE SellsItems LIKE ?");
            pstmt.setString(1, "%" + material.toString() + "%");
            ResultSet rs = pstmt.executeQuery();

            if (rs.isBeforeFirst()) {
                player.sendMessage(ChatColor.DARK_AQUA + "======= Showing shops selling " + material + " =======");
                while (rs.next()) {
                    try {

                        OfflinePlayer seller = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("OwnerUUID")));
                        Location loc = Location.deserialize(this.gson.fromJson(rs.getString("Location"), new TypeToken<Map<String, Object>>(){}.getType()));
                        ItemStack payment = BukkitSerialization.itemStackFromBase64(rs.getString("Payment"));

                        String paymentSTR = ChatColor.YELLOW + "" + payment.getAmount() + "x " + payment.getType();
                        if (payment.getAmount() == 0)
                            paymentSTR = ChatColor.GREEN + "free!";

                        String distance = "";
                        if (Objects.requireNonNull(player.getLocation().getWorld()).equals(loc.getWorld()))
                            distance = "(" + (int) loc.distance(player.getLocation()) + "m) ";

                        player.sendMessage(distance + ChatColor.YELLOW + seller.getName() + "'s" + ChatColor.RESET + " shop @ " + ChatColor.YELLOW + this.main.utility.locationAsString(loc) + ChatColor.RESET + " for " + paymentSTR);

                    } catch (IOException e) {
                        throw new ArithmeticException("[RizzCraft] findShopsSelling() failed to serialize: " + e.getMessage());
                    }
                }
            } else {
                player.sendMessage(this.main.prefix + ChatColor.YELLOW + "There are no shops selling this item, why not be the first, and setup your own shop with /shop create?");
            }

            rs.close();
            pstmt.close();
            con.close();
        } catch (SQLException e) {
            throw new ArithmeticException("[RizzCraft] findShopsSelling() failed to find query: " + e.getMessage());
        }
    }

    public boolean isValidInstance(BlockState blockState) {
        return blockState instanceof Chest || blockState instanceof Barrel || blockState instanceof ShulkerBox;
    }

    public void makeTransaction(Shop shop, Player buyer, int itemIndex) {

        if (!(shop.OwnerUID.equals(buyer.getUniqueId()))) {

            if (shop.Stock != null && shop.Stock[itemIndex] != null) {

                ItemStack item = shop.Stock[itemIndex].clone();
                shop.Stock[itemIndex] = null;

                if (shop.Payment != null) {

                    if (buyer.getInventory().containsAtLeast(shop.Payment, shop.Payment.getAmount())) {
                        this.main.logger.log(Level.INFO, "makeTransaction() selling Item: " + item + " to " + buyer.getName() + " for " + shop.Payment.toString());
                        buyer.getInventory().removeItem(shop.Payment);

                        shop.PaymentCount++;

                        Container container = (Container) shop.Location.getBlock().getState();
                        container.getInventory().setItem(itemIndex, null);

                        this.saveShop(shop);

                        buyer.getWorld().dropItemNaturally(buyer.getLocation(), item);

                        buyer.sendMessage(this.main.prefix + ChatColor.GREEN + "Item purchased successfully!");

                        OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.OwnerUID);
                        if (owner.isOnline()) {
                            Player o = (Player) owner;
                            Objects.requireNonNull(o).playSound(o.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100, -100);
                            o.sendMessage(
                                    this.main.prefix + ChatColor.GOLD + buyer.getName() + ChatColor.GREEN + " purchased " +
                                            ChatColor.GOLD + item.getAmount() + "x " + item.getType() + ChatColor.GREEN + " from your shop " +
                                            ChatColor.GOLD + shop.getName()
                            );
                        }

                        this.main.utility.alertMods(this.main.prefix + ChatColor.GRAY + buyer.getName() + " purchased " +
                                item.getAmount() + "x " + item.getType() + " from " + owner.getName() + "'s shop at "  +
                                this.main.utility.locationAsString(shop.Location));

                        try {
                            this.logTransaction(shop.ID, buyer, item, shop.Payment);
                        } catch (Exception e) {
                            this.main.logger.log(Level.SEVERE, "Failed to log shop transaction");
                            this.main.logger.log(Level.SEVERE, e.getMessage());
                        }

                    } else {
                        buyer.sendMessage(this.main.prefix + ChatColor.RED + "You cannot afford that item");
                        buyer.closeInventory();
                        buyer.updateInventory();
                        return;
                    }
                }

            } else {
                buyer.sendMessage(this.main.prefix + ChatColor.RED + "You cannot buy what the shop does not sell");
            }

            buyer.closeInventory();
            buyer.updateInventory();
        }
    }

    public void logTransaction(int shopID, Player buyer, ItemStack item, ItemStack payment) {
        try (Connection con = this.main.sql.connectionPool.getConnection()) {
            PreparedStatement pstmt = con.prepareStatement("INSERT INTO shops_transactions (ShopID,BuyerUUID,Item,Payment) VALUES (?,?,?,?)");
            pstmt.setInt(1, shopID);
            pstmt.setString(2, buyer.getUniqueId().toString());
            pstmt.setString(3, BukkitSerialization.itemStackToBase64(item));
            pstmt.setString(4, BukkitSerialization.itemStackToBase64(payment));
            pstmt.executeUpdate();
            pstmt.close();
            con.close();
            this.main.logger.log(Level.INFO, "logTransaction() completed for shop " + shopID);
        } catch (SQLException e) {
            throw new ArithmeticException("[RizzCraft] logTransaction() failed to log: " + e.getMessage());
        }
    }

    public void listTransactions(Player player, Shop shop) {

        try (Connection con = this.main.sql.connectionPool.getConnection()) {
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM (SELECT * FROM shops_transactions WHERE ShopID=? LIMIT 20) as output ORDER BY Timestamp ASC");
            pstmt.setInt(1, shop.ID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.isBeforeFirst()) {
                player.sendMessage(ChatColor.DARK_AQUA + "======= Showing last 20 transactions =======");
                while (rs.next()) {
                    try {
                        OfflinePlayer buyer = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("BuyerUUID")));
                        ItemStack item = BukkitSerialization.itemStackFromBase64(rs.getString("Item"));
                        ItemStack payment = BukkitSerialization.itemStackFromBase64(rs.getString("Payment"));

                        player.sendMessage(
                                ChatColor.DARK_AQUA + rs.getString("Timestamp").substring(5) + " " + ChatColor.YELLOW + buyer.getName() + ChatColor.RESET + " bought " +
                                        ChatColor.YELLOW + item.getAmount() + "x " + item.getType() +
                                        ChatColor.RESET + " for " + ChatColor.YELLOW + payment.getAmount() + "x " + payment.getType());

                    } catch (IOException e) {
                        throw new ArithmeticException("[RizzCraft] listTransactions() failed to serialize shop " + shop.ID + " - " + e.getMessage());
                    }
                }
            } else {
                player.sendMessage(this.main.prefix + ChatColor.RED + "No transactions available for this shop");
            }

            rs.close();
            pstmt.close();
            con.close();

        } catch (SQLException e) {
            throw new ArithmeticException("[RizzCraft] listTransactions() failed to query shop transactions for shop " + shop.ID + " - " + e.getMessage());
        }
    }

    public void collectPayments(Shop shop) {
        Player player = Bukkit.getOfflinePlayer(shop.OwnerUID).isOnline() ? Bukkit.getPlayer(shop.OwnerUID) : null;

        if (player !=  null) {

            int fireworks = 0;
            try (Connection con = this.main.sql.connectionPool.getConnection()) {
                PreparedStatement pstmt = con.prepareStatement("SELECT ShopFireworks from players where UUID=?");
                pstmt.setString(1, player.getUniqueId().toString());
                ResultSet rs = pstmt.executeQuery();

                if (rs.next())
                    fireworks = rs.getInt("ShopFireworks");

                rs.close();
                pstmt.close();
                con.close();
            } catch (SQLException e) {
                throw new ArithmeticException("[RizzCraft] Failed to check player firework settings: " + e.getMessage());
            }

            player.sendMessage(this.main.prefix + ChatColor.GOLD + "Congrats! Your shop has sold " + ChatColor.GREEN + shop.PaymentCount + ChatColor.GOLD + " items!");
            for (int i = 0; i < shop.PaymentCount; i++) {
                if (shop.Payment.getType() != Material.AIR)
                    Bukkit.getScheduler().runTaskLater(this.main, () -> player.getWorld().dropItemNaturally(player.getLocation(), shop.Payment), i * 10L);
                if (fireworks > 0) {
                    Bukkit.getScheduler().runTaskLater(this.main, () -> {
                        Firework fw = (Firework) player.getWorld().spawnEntity(shop.Location.clone().add(new Vector(0.5,1,0.5)), EntityType.FIREWORK);
                        FireworkMeta fwm = fw.getFireworkMeta();
                        fw.setCustomName("_nodamage_");
                        fwm.addEffect(FireworkEffect.builder().withColor(Color.GREEN).withColor(Color.YELLOW).flicker(true).build());
                        fwm.setPower(0);
                        fw.setFireworkMeta(fwm);
                        fw.detonate();
                    }, i * 10L);
                }
            }
            shop.PaymentCount = 0;
            this.saveShop(shop);
        }
    }

    public void toggleFireworks(Player player) {
        try (Connection con = this.main.sql.connectionPool.getConnection()) {
            PreparedStatement pstmt = con.prepareStatement("UPDATE players SET ShopFireworks = 1 - ShopFireworks WHERE UUID=?");
            pstmt.setString(1, player.getUniqueId().toString());
            pstmt.executeUpdate();
            pstmt.close();

            PreparedStatement pstmt0 = con.prepareStatement("SELECT ShopFireworks from players where UUID=?");
            pstmt0.setString(1, player.getUniqueId().toString());
            ResultSet rs = pstmt0.executeQuery();

            String setting = ChatColor.GREEN + "on";
            if (rs.next() && rs.getInt("ShopFireworks") == 0) {
                setting = ChatColor.RED + "off";
            }

            rs.close();
            pstmt0.close();
            con.close();
            player.sendMessage(this.main.prefix + ChatColor.YELLOW + "Shop fireworks have been toggled: " + setting);
        } catch (SQLException e) {
            throw new ArithmeticException("[RizzCraft] Failed to update player firework settings: " + e.getMessage());
        }
    }

    public void collectAllPayments(Player player) {
        ArrayList<Shop> shops = this.getShopsByPlayer(player);
        boolean wasPayments = false;

        if (shops != null && shops.size() > 0) {
            for (Shop shop : shops) {
                if (shop.PaymentCount > 0) {
                    for (int i = 0; i < shop.PaymentCount; i++) {
                        Bukkit.getScheduler().runTaskLater(this.main, () -> player.getWorld().dropItemNaturally(player.getLocation(), shop.Payment), 300 + i * 10L);
                        Bukkit.getScheduler().runTaskLater(this.main, () -> player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1), 300 + i * 10L);
                    }
                    this.main.logger.log(Level.INFO, "CollectAllPayments() payment delivery scheduled from shop ID " + shop.ID + " for player " + Bukkit.getOfflinePlayer(shop.OwnerUID).getName());

                    shop.PaymentCount = 0;
                    this.saveShop(shop);
                    wasPayments = true;
                }
            }
            if (wasPayments) {
                player.sendMessage(this.main.prefix + ChatColor.GREEN + "There have been payments made in some of your shops! Payment is on the way! (15s)");
                Bukkit.getScheduler().runTaskLater(this.main, () -> player.sendMessage(this.main.prefix + ChatColor.GREEN + "Your items have arrived!"), 300 + 0 * 10);
            } else {
                player.sendMessage(this.main.prefix + ChatColor.YELLOW + "There are no payments to collect from any of your shops");
            }
        }
    }

    public ArrayList<Shop> getShopsByPlayer(Player player) {
        ArrayList<Shop> shopList;

        try (Connection con = this.main.sql.connectionPool.getConnection()) {
            PreparedStatement pstmt = con.prepareStatement("SELECT * from shops WHERE OwnerUUID=?", ResultSet.CONCUR_UPDATABLE);
            pstmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = pstmt.executeQuery();

            shopList = this.shopsFromSQL(rs);

            rs.close();
            pstmt.close();
            con.close();

        } catch (SQLException e) {
            throw new ArithmeticException("[RizzCraft] Failed to getShopsByPlayer() for player " + player.getUniqueId() + " - " + e.getMessage());
        }

        if (shopList != null && shopList.size() == 0)
            return null;
        else
            this.main.logger.log(Level.INFO, "getShopsByPlayer() returned " + Objects.requireNonNull(shopList).size() + " for player " + player.getUniqueId());

        return shopList;
    }

    public void handleBlockBreakEvent(BlockBreakEvent event) {
        if (this.isValidInstance(event.getBlock().getState())) {
            Shop shop = this.getShop(event.getBlock().getLocation());
            if (shop != null) {
                if (shop.OwnerUID.equals(event.getPlayer().getUniqueId())) {
                    this.deleteShop(shop.ID);
                    Objects.requireNonNull(Bukkit.getPlayer(event.getPlayer().getUniqueId())).sendMessage(this.main.prefix + ChatColor.YELLOW + "Shop Destroyed at " + this.main.utility.locationAsString(shop.Location));
                } else {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(this.main.prefix + ChatColor.RED + "This shop does not belong to you");
                }
            }
        }
    }

    public void setQuantity(Shop shop, Integer amount) {
        Player player = Bukkit.getOfflinePlayer(shop.OwnerUID).isOnline() ? Bukkit.getPlayer(shop.OwnerUID) : null;
        shop.Quantity = amount;
        this.saveShop(shop);
        if (player != null) {
            player.sendMessage(this.main.prefix + ChatColor.GREEN + "Your shops items per transaction has been updated to: " + ChatColor.YELLOW + amount);
        }
    }

    public void nameShop(Player player, Shop shop, String name) {
        if (name.length() > 50) {
            player.sendMessage(this.main.prefix + ChatColor.RED + "Please use a shorter name. Max 30 characters");
        } else {
            shop.shopName = name;
            this.saveShop(shop);
            player.sendMessage(this.main.prefix + ChatColor.GREEN + "This shop has successfully been named \"" + name + "\"");
        }
    }
}