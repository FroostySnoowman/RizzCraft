package rizzcraft.net.rizzcraft.Welcomer;

import rizzcraft.net.rizzcraft.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener extends Event implements Listener {
    private final Welcomer welcomer;
    public final Main main;

    public EventListener(Welcomer welcomer, Main main) {
        this.welcomer = welcomer;
        this.main = main;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        if (!this.welcomer.main.sql.PlayerExistsInDB(event.getPlayer())) {
            this.welcomer.main.sql.CreatePlayerDBEntry(event.getPlayer());
        }
        this.welcomer.check(event.getPlayer());
        this.welcomer.updateName(event.getPlayer());
        this.welcomer.looksLikeALT(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent event) {
        this.welcomer.updateStats(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && this.welcomer.selectionOptions.contains(event.getCurrentItem())) {
            Player target = (Player)event.getWhoClicked();
            target.closeInventory();
            target.getInventory().clear();
            event.getCurrentItem();
            if (event.getCurrentItem().equals(this.welcomer.selectionOptions.get(1))) {
                target.getInventory().addItem(new ItemStack[]{new ItemStack(Material.COOKED_BEEF, 16)});
                target.getInventory().addItem(new ItemStack[]{new ItemStack(Material.RED_BED, 1)});
            }

            if (event.getCurrentItem().equals(this.welcomer.selectionOptions.get(2))) {
                target.getInventory().addItem(new ItemStack[]{new ItemStack(Material.STONE_AXE, 1)});
                target.getInventory().addItem(new ItemStack[]{new ItemStack(Material.STONE_PICKAXE, 1)});
                target.getInventory().addItem(new ItemStack[]{new ItemStack(Material.COOKED_BEEF, 16)});
                target.getInventory().addItem(new ItemStack[]{new ItemStack(Material.RED_BED, 1)});
            }

            this.welcomer.welcome(target, 2);
        }
    }

    public HandlerList getHandlers() {
        return null;
    }
}