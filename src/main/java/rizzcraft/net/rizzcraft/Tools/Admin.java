package rizzcraft.net.rizzcraft.Tools;

import java.util.ArrayList;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public class Admin {
    private final Tools plugin;
    public final ArrayList<Player> activeAdminChat;

    public Admin(Tools plugin) {
        this.plugin = plugin;
        this.activeAdminChat = new ArrayList();
    }

    public void toggleAdminChat(Player player) {
        if (this.activeAdminChat.contains(player)) {
            this.activeAdminChat.remove(player);
            player.sendMessage(this.plugin.main.prefix + "Admin chat " + ChatColor.RED + "disabled");
        } else {
            this.activeAdminChat.add(player);
            player.sendMessage(this.plugin.main.prefix + "Admin chat " + ChatColor.GREEN + "enabled");
        }
    }
}