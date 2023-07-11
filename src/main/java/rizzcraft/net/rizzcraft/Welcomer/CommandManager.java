package rizzcraft.net.rizzcraft.Welcomer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor {

    public final Welcomer plugin;

    public CommandManager(Welcomer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.main.prefix + ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("spawn")) {
            plugin.main.utility.TeleportPlayerInXSeconds(player, 3, 0, 64, 0);
        }

        if (label.equalsIgnoreCase("ping")) {
            int ping = player.getPing();
            if (ping == 0) {
                player.sendMessage(plugin.main.prefix + ChatColor.RED + "Please wait while your ping initializes!");
            } else {
                player.sendMessage(plugin.main.prefix + "Your ping is: " + ping + "ms");
            }
        }

        return true;
    }
}