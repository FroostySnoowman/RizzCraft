package rizzcraft.net.rizzcraft.Death;

import rizzcraft.net.rizzcraft.Main;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class Messages {

    private final Main main;

    public Messages(Main main) {
        this.main = main;
    }

    public boolean mapContainsKey(String searchKey) {
        return this.main.deathMessageData.getConfig().contains(searchKey);
    }

    public String getRandomMessageFromKey(String searchKey) {
        List<?> messages = this.main.deathMessageData.getConfig().getList(searchKey);
        Random rnd = new Random();
        assert messages != null;
        int num = rnd.nextInt(messages.size());
        return (String) messages.get(num);
    }

    public String decideBestMessage(DamageCause cause, Material block, Entity entity) {
        String msg = null;

        if (mapContainsKey(cause.toString())) {

            if (block != null && this.mapContainsKey(cause + "." + block + ".MESSAGES")) {
                msg = this.getRandomMessageFromKey(cause + "." + block + ".MESSAGES");
            }

            else if (entity != null && this.mapContainsKey(cause + "." + entity.getType() + ".MESSAGES")) {
                msg = this.getRandomMessageFromKey(cause + "." + entity.getType() + ".MESSAGES");
            }

            else if (entity != null && entity instanceof Mob && this.mapContainsKey(cause + ".MOB.MESSAGES")) {
                msg = this.getRandomMessageFromKey(cause + ".MOB.MESSAGES");
            }

            else if (this.mapContainsKey(cause + ".MESSAGES")) {
                msg = this.getRandomMessageFromKey(cause + ".MESSAGES");
            }

            return msg;
        } else {
            return null;
        }
    }

    public TextComponent[] formatMessage(String msg, Player player, Material block, Entity entity, Player killer, ItemStack item) {
        String[] words = msg.split(" ");
        List<TextComponent> output = new ArrayList<TextComponent>();

        for (int i = 0; i < words.length; i++) {

            if (player != null && words[i].contains("{{player}}")) {
                output.add(new TextComponent(words[i].replace("{{player}}", player.getName())));
            }

            else if (entity != null && words[i].contains("{{mob}}")) {
                output.add(new TextComponent(words[i].replace("{{mob}}", entity.getName())));
            }

            else if (killer != null && words[i].contains("{{killer}}")) {
                output.add(new TextComponent(words[i].replace("{{killer}}", killer.getName())));
            }

            else if (block != null && words[i].contains("{{block}}")) {
                output.add(new TextComponent(words[i].replace("{{block}}", block.toString())));
            }

            else if (item != null && words[i].contains("{{item}}") && !item.getType().equals(Material.AIR)) {
                TextComponent itemText = new TextComponent();
                if (Objects.requireNonNull(item.getItemMeta()).hasDisplayName()) {
                    itemText.setText("[" + item.getItemMeta().getDisplayName() + "]");
                } else {
                    itemText.setText(item.getType().toString());
                }
                output.add(itemText);
            }

            else {
                output.add(new TextComponent(words[i]));
            }
        }

        for (TextComponent txt : output) {
            txt.setText(txt.getText() + " ");
            txt.setColor(ChatColor.RESET);
        }

        TextComponent[] result = new TextComponent[output.size()];
        return output.toArray(result);
    }

    public void handleEvent(PlayerDeathEvent event) {

        Player player = event.getEntity();
        DamageCause cause;
        Material block = null;
        Entity entity = null;
        Projectile projectile = null;
        Player killer = null;
        ItemStack item = null;

        Objects.requireNonNull(event.getEntity().getLastDamageCause()).getCause();
        cause = event.getEntity().getLastDamageCause().getCause();

        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ent = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
            entity = ent.getDamager();

            if (entity instanceof Projectile) {
                projectile = (Projectile) entity;
                entity = (Entity) projectile.getShooter();
            }

            if (entity instanceof Player) {
                killer = (Player) entity;
                item = killer.getInventory().getItemInMainHand();
            }

            if (entity instanceof FallingBlock) {
                FallingBlock fallingBlock = (FallingBlock) entity;
                block = fallingBlock.getBlockData().getMaterial();
            }
        }

        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByBlockEvent) {
            EntityDamageByBlockEvent blk = (EntityDamageByBlockEvent) event.getEntity().getLastDamageCause();
            if (blk.getDamager() != null) {
                block = blk.getDamager().getType();
            }
        }

        Random r = new Random();
        int result = r.nextInt(100);
        if (result >= 50) {
            String msg = this.decideBestMessage(cause, block, entity);
            if (msg != null) {
                event.setDeathMessage("");
                this.main.getServer().spigot().broadcast(this.formatMessage(msg, player, block, entity, killer, item));
            }
        }

    }
}