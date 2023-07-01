package rizzcraft.net.rizzcraft.Welcomer;
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
        if (label.equalsIgnoreCase("spawn")) {
            Player p = (Player) sender;
            plugin.main.utility.TeleportPlayerInXSeconds(p, 3, 0, 64, 0);
        }
        return true;
    }
}