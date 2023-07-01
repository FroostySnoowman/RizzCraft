package rizzcraft.net.rizzcraft.Death;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.Objects;

public class EventListener extends Event implements Listener {
    private final DeathLogger module;

    public EventListener(DeathLogger module) {
        this.module = module;
    }

    @EventHandler
    public void OnPlayerDeathEvent(PlayerDeathEvent event) {
        this.module.logDeath(event);
        
        if (event.getEntity().getKiller() != null) {
            if (event.getEntity().getKiller().getInventory().getItemInMainHand().getType().toString().contains("SWORD")) {
                ItemStack sword = event.getEntity().getKiller().getInventory().getItemInMainHand();

                double number = Math.random();
                double dropchance = 0.007;
                if (sword.getEnchantments().get(Enchantment.SWEEPING_EDGE) != null) {
                    dropchance = 0.04;
                }

                if (dropchance > number) {
                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) head.getItemMeta();
                    assert meta != null;
                    meta.setOwningPlayer(event.getEntity());
                    head.setItemMeta(meta);

                    Objects.requireNonNull(event.getEntity().getLocation().getWorld()).dropItemNaturally(event.getEntity().getLocation(), head);
                }
            }
        }
    }

    public HandlerList getHandlers() {
        return null;
    }
}