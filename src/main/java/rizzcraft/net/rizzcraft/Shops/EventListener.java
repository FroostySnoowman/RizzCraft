package rizzcraft.net.rizzcraft.Shops;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EventListener extends Event implements Listener {
    private final ShopHandler shopHandler;

    public EventListener(ShopHandler shopHandler) {
        this.shopHandler = shopHandler;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ArrayList<Shop> shopList = this.shopHandler.getShopsByPlayer(player);
        ArrayList<Shop> noneEmptyShops = new ArrayList<>();
        if (shopList != null) {
            for(Shop shop : shopList) {
                if (shop != null) {
                    for(ItemStack item : shop.Stock) {
                        if (item != null) {
                            noneEmptyShops.add(shop);
                        }
                    }
                }
            }

            for(Shop shop : noneEmptyShops) {
                shopList.remove(shop);
            }

            for(Shop shop : shopList) {
                player.sendMessage(new String[0]);
                Bukkit.getScheduler()
                        .runTaskLater(
                                this.shopHandler.main,
                                () -> player.sendMessage(
                                        this.shopHandler.main.prefix
                                                + ChatColor.YELLOW
                                                + "Your shop at "
                                                + this.shopHandler.main.utility.locationAsString(shop.Location)
                                                + " is empty"
                                ),
                                60L
                        );
            }

            try {
                PreparedStatement pstmt = this.shopHandler
                        .main
                        .sql
                        .connectionPool
                        .getConnection()
                        .prepareStatement("SELECT * FROM shops WHERE OwnerUUID=? AND PaymentCount>0");
                pstmt.setString(1, player.getUniqueId().toString());
                ResultSet rs = pstmt.executeQuery();
                if (rs.isBeforeFirst()) {
                    Bukkit.getScheduler()
                            .runTaskLater(
                                    this.shopHandler.main,
                                    () -> player.sendMessage(this.shopHandler.main.prefix + ChatColor.GREEN + "Your shops have sold items! Collect payments with /shop collect"),
                                    60L
                            );
                }

                rs.close();
                pstmt.close();
            } catch (SQLException var11) {
                var11.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        if (this.shopHandler.PlayersInteracting.containsKey(event.getPlayer())) {
            Player player = (Player)event.getPlayer();
            String[] args = (String[])this.shopHandler.PlayersInteracting.get(player);
            this.shopHandler.PlayersInteracting.remove(player);
            event.setCancelled(true);
            Shop shop = this.shopHandler.getShop(Objects.requireNonNull(event.getInventory().getLocation()));
            if (args[0].equals("CREATE")) {
                if (shop == null) {
                    if (event.getInventory().getSize() > 27) {
                        player.sendMessage(this.shopHandler.main.prefix + ChatColor.RED + "Shop cannot be a double chest");
                    } else if (args.length > 1) {
                        UUID playerID = this.shopHandler.main.utility.getPlayerUUID(args[1].toLowerCase());
                        if (playerID == null) {
                            player.sendMessage(this.shopHandler.main.prefix + "That player doesn't exist.");
                        }

                        Shop s = this.shopHandler.createShop(player, event.getInventory().getLocation());
                        s.OwnerUID = playerID;
                        this.shopHandler.saveShop(s);
                    } else {
                        this.shopHandler.createShop(player, event.getInventory().getLocation());
                    }
                } else {
                    event.getPlayer()
                            .sendMessage(
                                    this.shopHandler.main.prefix
                                            + ChatColor.YELLOW
                                            + "This is already a shop belonging to "
                                            + Bukkit.getOfflinePlayer(shop.OwnerUID).getName()
                            );
                }
            }

            if (shop == null) {
                event.getPlayer().sendMessage(this.shopHandler.main.prefix + ChatColor.YELLOW + "That isn't a registered shop");
                return;
            }

            if (args[0].equals("INFO")) {
                event.getPlayer()
                        .sendMessage(this.shopHandler.main.prefix + ChatColor.GOLD + "Owner: " + ChatColor.YELLOW + Bukkit.getOfflinePlayer(shop.OwnerUID).getName());
                event.getPlayer().sendMessage(this.shopHandler.main.prefix + ChatColor.GOLD + "Shop ID: " + ChatColor.YELLOW + shop.ID);
                event.getPlayer()
                        .sendMessage(
                                this.shopHandler.main.prefix + ChatColor.GOLD + "Shop Name: " + ChatColor.YELLOW + (shop.shopName != null ? shop.shopName : "None")
                        );
                event.getPlayer()
                        .sendMessage(
                                this.shopHandler.main.prefix
                                        + ChatColor.GOLD
                                        + "Location: "
                                        + ChatColor.YELLOW
                                        + this.shopHandler.main.utility.locationAsString(shop.Location)
                        );
                if (shop.Payment != null) {
                    event.getPlayer()
                            .sendMessage(
                                    this.shopHandler.main.prefix
                                            + ChatColor.GOLD
                                            + "Payment: "
                                            + ChatColor.YELLOW
                                            + shop.Payment.getAmount()
                                            + "x "
                                            + shop.Payment.getType().name()
                            );
                } else {
                    event.getPlayer().sendMessage(this.shopHandler.main.prefix + ChatColor.GOLD + "Payment: " + ChatColor.YELLOW + "Free!");
                }

                event.getPlayer().sendMessage(this.shopHandler.main.prefix + ChatColor.GOLD + "Items per transaction: " + ChatColor.YELLOW + shop.Quantity);
            }

            if (args[0].equals("TRANSFER")) {
                if (!shop.OwnerUID.equals(event.getPlayer().getUniqueId()) && !event.getPlayer().hasPermission("rc.mod")) {
                    event.getPlayer()
                            .sendMessage(
                                    this.shopHandler.main.prefix
                                            + ChatColor.YELLOW
                                            + "You can't transfer this as it belongs to "
                                            + Bukkit.getOfflinePlayer(this.shopHandler.getShop(event.getInventory().getLocation()).OwnerUID).getName()
                            );
                } else {
                    this.shopHandler.transferOwnership(shop, player, Bukkit.getOfflinePlayer(UUID.fromString(args[1])));
                }
            }

            if (args[0].equals("DELETE")) {
                if (!shop.OwnerUID.equals(event.getPlayer().getUniqueId()) && !event.getPlayer().hasPermission("rc.mod")) {
                    event.getPlayer()
                            .sendMessage(
                                    this.shopHandler.main.prefix
                                            + ChatColor.YELLOW
                                            + "Sorry, but this shop belongs to "
                                            + Bukkit.getOfflinePlayer(this.shopHandler.getShop(event.getInventory().getLocation()).OwnerUID).getName()
                            );
                } else if (this.shopHandler.deleteShop(shop.ID)) {
                    if (Bukkit.getOfflinePlayer(event.getPlayer().getUniqueId()).isOnline()) {
                        ((Player)Objects.requireNonNull(Bukkit.getPlayer(event.getPlayer().getUniqueId())))
                                .sendMessage(
                                        this.shopHandler.main.prefix + ChatColor.YELLOW + "Shop Destroyed at " + this.shopHandler.main.utility.locationAsString(shop.Location)
                                );
                    }
                } else if (Bukkit.getOfflinePlayer(event.getPlayer().getUniqueId()).isOnline()) {
                    ((Player)Objects.requireNonNull(Bukkit.getPlayer(event.getPlayer().getUniqueId())))
                            .sendMessage(this.shopHandler.main.prefix + ChatColor.RED + "Error deleting shop");
                }
            }

            if (args[0].equals("NAME")) {
                if (!shop.OwnerUID.equals(event.getPlayer().getUniqueId()) && !event.getPlayer().hasPermission("rc.mod")) {
                    event.getPlayer()
                            .sendMessage(
                                    this.shopHandler.main.prefix
                                            + ChatColor.YELLOW
                                            + "Sorry, but this shop belongs to "
                                            + Bukkit.getOfflinePlayer(this.shopHandler.getShop(event.getInventory().getLocation()).OwnerUID).getName()
                            );
                } else {
                    this.shopHandler.nameShop(player, shop, args[1]);
                }
            }

            if (args[0].equals("TRANSACTION")) {
                this.shopHandler.listTransactions(player, shop);
                event.setCancelled(true);
            }

            if (args[0].equals("QUANTITY")) {
                if (!shop.OwnerUID.equals(event.getPlayer().getUniqueId()) && !event.getPlayer().hasPermission("rc.mod")) {
                    event.getPlayer()
                            .sendMessage(
                                    this.shopHandler.main.prefix
                                            + ChatColor.YELLOW
                                            + "Sorry, but this shop belongs to "
                                            + Bukkit.getOfflinePlayer(this.shopHandler.getShop(event.getInventory().getLocation()).OwnerUID).getName()
                            );
                } else {
                    int amount = Integer.parseInt(args[1]);
                    this.shopHandler.setQuantity(shop, amount);
                    event.setCancelled(true);
                }
            }
        }

        if (event.getInventory().getLocation() != null && this.shopHandler.isValidInstance(event.getInventory().getLocation().getBlock().getState())) {
            Player player = (Player)event.getPlayer();
            if (this.shopHandler.lockedChests) {
                event.setCancelled(true);
                player.sendMessage(this.shopHandler.main.prefix + ChatColor.RED + "Chests are currently locked server-wide.");
                this.shopHandler
                        .main
                        .utility
                        .alertMods(this.shopHandler.main.prefix + ChatColor.GRAY + player.getName() + " tried to access a CHEST, but are locked globally");
            } else {
                Shop shop = this.shopHandler.getShop(event.getInventory().getLocation());
                if (shop != null) {
                    if (this.shopHandler.lockedShops) {
                        event.setCancelled(true);
                        player.sendMessage(this.shopHandler.main.prefix + ChatColor.RED + "Shops are currently locked server-wide.");
                        this.shopHandler
                                .main
                                .utility
                                .alertMods(this.shopHandler.main.prefix + ChatColor.GRAY + player.getName() + " tried to access a SHOP, but are locked globally");
                    } else if (shop.OwnerUID.equals(player.getUniqueId())) {
                        if (shop.PaymentCount > 0 && shop.Payment != null) {
                            event.setCancelled(true);
                            this.shopHandler.collectPayments(shop);
                        } else {
                            player.sendMessage(this.shopHandler.main.prefix + ChatColor.GOLD + "This is your shop");
                            shop.Locked = true;
                            this.shopHandler.saveShop(shop);
                        }
                    } else {
                        event.setCancelled(true);
                        if (shop.Locked) {
                            player.sendMessage(this.shopHandler.main.prefix + ChatColor.RED + "This shop is locked currently");
                        } else {
                            Inventory menu = shop.getBuyerFriendlyInventory();
                            player.openInventory(menu);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        if (event.getInventory().getLocation() != null && this.shopHandler.isValidInstance(event.getInventory().getLocation().getBlock().getState())) {
            Player player = (Player)event.getPlayer();
            if (this.shopHandler.lockedChests) {
                this.shopHandler
                        .main
                        .utility
                        .alertMods(this.shopHandler.main.prefix + ChatColor.GRAY + player.getName() + " tried to access a CHEST, but are locked globally");
            } else {
                Shop shop = this.shopHandler.getShop(event.getInventory().getLocation());
                if (shop != null && shop.OwnerUID.equals(player.getUniqueId()) && event.getInventory().getContents() != shop.Stock) {
                    shop.UpdateStock();
                    shop.Locked = false;
                    this.shopHandler.saveShop(shop);
                    player.sendMessage(this.shopHandler.main.prefix + ChatColor.GREEN + "Stock Updated");
                }
            }
        }
    }

    @EventHandler(
            priority = EventPriority.HIGH
    )
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (event.getView().getTitle().toLowerCase().contains("shop")) {
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
                if (event.getAction() == InventoryAction.PLACE_ALL
                        || event.getAction() == InventoryAction.PLACE_ONE
                        || event.getAction() == InventoryAction.PLACE_SOME) {
                    event.setCancelled(true);
                }

                if (event.getCursor() != null
                        && event.getClickedInventory() != null
                        && event.getCurrentItem() != null
                        && event.getClickedInventory().getHolder() != null
                        && event.getClickedInventory().getHolder().getInventory().getLocation() != null
                        && this.shopHandler.isValidInstance(event.getClickedInventory().getHolder().getInventory().getLocation().getBlock().getState())
                        && this.shopHandler.getShop(event.getClickedInventory().getHolder().getInventory().getLocation()) != null) {
                    Shop shop = this.shopHandler.getShop(event.getClickedInventory().getHolder().getInventory().getLocation());
                    if (shop.Stock[event.getSlot()] != null && !shop.Locked) {
                        this.shopHandler.makeTransaction(shop, (Player)event.getWhoClicked(), event.getSlot());
                    } else {
                        event.setCancelled(true);
                        Player player = (Player)event.getWhoClicked();
                        player.closeInventory();
                        player.updateInventory();
                        player.sendMessage(this.shopHandler.main.prefix + ChatColor.RED + "This shop is locked currently");
                    }
                }
            }

            if (event.getClickedInventory() != null
                    && event.getClickedInventory().equals(event.getView().getBottomInventory())
                    && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDragEvent(InventoryDragEvent event) {
        if (event.getView().getTitle().toLowerCase().contains("shop")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (event.getBlock().getBlockData() instanceof Chest) {
            Block block = event.getBlock();
            boolean printmssg = false;

            for(BlockFace dir : Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)) {
                if (this.shopHandler.getShop(block.getRelative(dir).getLocation()) != null) {
                    Shop shop = this.shopHandler.getShop(block.getRelative(dir).getLocation());
                    Chest blockData = (Chest)block.getBlockData();
                    blockData.setType(Type.SINGLE);
                    event.getBlock().setBlockData(blockData);
                    shop.Location.getBlock().getState().update();
                    printmssg = true;
                }
            }

            if (printmssg) {
                event.getPlayer().sendMessage(this.shopHandler.main.prefix + ChatColor.YELLOW + "Shops cannot be double chests");
                printmssg = false;
            }
        }
    }

    @EventHandler
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        try {
            event.getInitiator();
            event.getSource();
            event.getDestination();
            if (event.getDestination().getLocation() != null && this.shopHandler.isValidInstance(event.getDestination().getLocation().getBlock().getState())) {
                Shop shop = this.shopHandler.getShop(event.getDestination().getLocation());
                if (shop != null) {
                    event.setCancelled(true);
                    Bukkit.getScheduler()
                            .runTask(
                                    this.shopHandler.main,
                                    () -> event.getInitiator().getLocation().getWorld().getBlockAt(event.getInitiator().getLocation()).breakNaturally()
                            );
                    Bukkit.getScheduler()
                            .runTask(
                                    this.shopHandler.main, () -> event.getSource().getLocation().getWorld().getBlockAt(event.getSource().getLocation()).breakNaturally()
                            );
                    ItemStack item = event.getItem();
                    Collection<Entity> entities = event.getSource().getLocation().getWorld().getNearbyEntities(event.getSource().getLocation(), 10.0, 10.0, 10.0);
                    this.shopHandler
                            .main
                            .logger
                            .log(
                                    Level.INFO,
                                    "AntiDupe - "
                                            + event.getInitiator().getType()
                                            + " @ "
                                            + this.shopHandler.main.utility.locationAsString(event.getInitiator().getLocation())
                                            + " initiated "
                                            + event.getSource().getType()
                                            + " @ "
                                            + this.shopHandler.main.utility.locationAsString(event.getSource().getLocation())
                                            + " to put item "
                                            + item
                                            + " into a shop: "
                                            + event.getDestination().getType()
                                            + " @ "
                                            + this.shopHandler.main.utility.locationAsString(event.getDestination().getLocation())
                            );

                    for(Entity ent : entities) {
                        if (ent.getType() == EntityType.PLAYER) {
                            this.shopHandler.main.logger.log(Level.INFO, "AntiDupe - Nearby player: " + ent.getName());
                            this.shopHandler
                                    .main
                                    .utility
                                    .alertMods(
                                            this.shopHandler.main.prefix
                                                    + ChatColor.GOLD
                                                    + ent.getName()
                                                    + ChatColor.RED
                                                    + " attempted a known dupe method @ "
                                                    + ChatColor.YELLOW
                                                    + this.shopHandler.main.utility.locationAsString(ent.getLocation())
                                    );
                        }
                    }
                }
            }
        } catch (Exception var7) {
            throw new ArithmeticException("[RizzCraft] InventoryMoveItemEvent() failed to complete: " + var7.getMessage());
        }
    }

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        Iterator<Block> it = event.blockList().iterator();

        while(it.hasNext()) {
            Block next = (Block)it.next();
            if (this.shopHandler.isValidInstance(next.getState()) && this.shopHandler.getShop(next.getLocation()) != null) {
                it.remove();
            }
        }
    }

    @EventHandler
    public void onBlockPistonEvent(BlockPistonExtendEvent event) {
        for(Block next : event.getBlocks()) {
            if (this.shopHandler.isValidInstance(next.getState()) && this.shopHandler.getShop(next.getLocation()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework
                && event.getEntity() instanceof Player
                && event.getDamager().getCustomName() != null
                && event.getDamager().getCustomName().equals("_nodamage_")) {
            event.setCancelled(true);
        }
    }

    public HandlerList getHandlers() {
        return null;
    }
}