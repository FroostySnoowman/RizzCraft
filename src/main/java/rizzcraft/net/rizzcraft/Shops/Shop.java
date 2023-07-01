package rizzcraft.net.rizzcraft.Shops;

import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Shop {
    public final int ID;
    public UUID OwnerUID;
    public String shopName;
    public final Location Location;
    public ItemStack[] Stock;
    public final ItemStack Payment;
    public int PaymentCount;
    public boolean Locked;
    public int Quantity;

    public Shop(int id, UUID ownerUID, String shopName, ItemStack[] stock, ItemStack payment, int paymentCount, Location location, boolean locked, int quantity) {
        this.ID = id;
        this.OwnerUID = ownerUID;
        this.shopName = shopName;
        this.Stock = stock;
        this.Payment = payment;
        this.PaymentCount = paymentCount;
        this.Location = location;
        this.Locked = locked;
        this.Quantity = quantity;
    }

    public Inventory getBuyerFriendlyInventory() {
        try {
            Inventory inv = Bukkit.createInventory(
                    (Container)this.Location.getBlock().getState(), 27, Bukkit.getOfflinePlayer(this.OwnerUID).getName() + "'s Shop"
            );
            ItemStack[] contents = new ItemStack[27];
            if (this.Stock != null) {
                for(int i = 0; i < this.Stock.length; ++i) {
                    if (this.Stock[i] != null) {
                        ItemStack itm = this.Stock[i].clone();
                        ItemMeta itmMeta = itm.getItemMeta().clone();
                        if (this.Payment != null) {
                            itmMeta.setLore(List.of("Click to purchase for " + this.Payment.getAmount() + "x " + this.Payment.getType().name()));
                        } else {
                            itmMeta.setLore(List.of("Click to purchase for FREE!"));
                        }

                        itm.setItemMeta(itmMeta);
                        contents[i] = itm;
                    }
                }
            }

            inv.setContents(contents);
            return inv;
        } catch (Exception var6) {
            throw new ArithmeticException("[RizzCraft] Failed to create friendly inventory: " + var6.getMessage());
        }
    }

    public String getName() {
        return this.shopName != null ? this.shopName : "@ " + this.Location.getBlockX() + ", " + this.Location.getBlockY() + ", " + this.Location.getBlockZ();
    }

    public void UpdateStock() {
        Block block = this.Location.getBlock();
        if (block.getState() instanceof Container cont) {
            this.Stock = cont.getInventory().getContents();
        }
    }
}