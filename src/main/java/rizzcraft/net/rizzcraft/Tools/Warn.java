package rizzcraft.net.rizzcraft.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Warn {
    final Tools rc;

    public Warn(Tools rc) {
        this.rc = rc;
    }

    public void WarnPlayer(Player player, String[] args) {
        Object target_ = this.rc.main.utility.getPlayer(player, args[1].toLowerCase());
        if (target_ instanceof Player target && ((Player)target_).isOnline()) {
            ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
            argsList.remove(0);
            argsList.remove(0);
            target.sendTitle(ChatColor.RED + "You have been warned.", ChatColor.RED + String.join(" ", argsList), 10, 300, 10);
            target.playSound(target.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0F, 1.0F);
            Bukkit.getScheduler().runTaskLater(this.rc.main, () -> target.sendTitle("", ChatColor.RED + "You will not be warned again.", 10, 250, 10), 320L);
            this.rc
                    .main
                    .utility
                    .alertMods(
                            this.rc.main.prefix
                                    + ChatColor.GOLD
                                    + player.getName()
                                    + ChatColor.RESET
                                    + " warned "
                                    + ChatColor.GOLD
                                    + target.getName()
                                    + ChatColor.RESET
                                    + " with: "
                                    + ChatColor.GRAY
                                    + String.join(" ", argsList)
                    );
        } else if (target_ instanceof OfflinePlayer) {
            player.sendMessage(this.rc.main.prefix + ChatColor.GOLD + ((OfflinePlayer)target_).getName() + ChatColor.RESET + " is offline.");
        }
    }
}