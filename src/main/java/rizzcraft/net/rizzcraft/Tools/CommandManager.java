package rizzcraft.net.rizzcraft.Tools;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatColor;

public class CommandManager implements CommandExecutor {

    private final Tools tools;

    public CommandManager(Tools plugin) {
        this.tools = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.tools.main.prefix + ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("rc") && sender instanceof Player && (sender.hasPermission("rc.use") || this.tools.admins.contains(((Player) sender).getUniqueId().toString()))) {

            if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                player.sendMessage(ChatColor.GREEN + "======== RizzCraft Help =======");
                player.sendMessage(ChatColor.GREEN + "/rc " + ChatColor.AQUA + "whois <player>");
                player.sendMessage(ChatColor.GREEN + "/rc " + ChatColor.AQUA + "ping");
                player.sendMessage(ChatColor.GREEN + "/rc " + ChatColor.AQUA + "warn <player> <reason>");
                player.sendMessage(ChatColor.GREEN + "/rc " + ChatColor.AQUA + "a ");
                player.sendMessage(ChatColor.GREEN + "/rc " + ChatColor.AQUA + "death search <player>");
                player.sendMessage(ChatColor.GREEN + "/rc " + ChatColor.AQUA + "death load <id>");
                return true;
            }

            if (args[0].equalsIgnoreCase("whois"))
                if (args.length == 2)
                    this.tools.whois.get(player, args);
                else {
                    player.sendMessage(this.tools.main.prefix + ChatColor.RED + "Please specify a player to get information for!");
                }

            if (args[0].equalsIgnoreCase("ping") && args.length == 1) {
                int ping = player.getPing();
                if (ping == 0) {
                    player.sendMessage(this.tools.main.prefix + ChatColor.RED + "Please wait while your ping initializes!");
                } else {
                    player.sendMessage(this.tools.main.prefix + "Your ping is: " + ping + "ms");
                }
            }

            if (args[0].equalsIgnoreCase("warn"))
                if (args.length >= 2) {
                    if (args.length >= 3) {
                        this.tools.warn.WarnPlayer(player, args);
                    } else {
                        player.sendMessage(this.tools.main.prefix + ChatColor.RED + "Please specify a warning for them!");
                    }
                } else {
                    player.sendMessage(this.tools.main.prefix + ChatColor.RED + "Please specify a player to warn!");
                }

            if (args[0].equalsIgnoreCase("a"))
                this.tools.admin.toggleAdminChat(player);

            if (args[0].equalsIgnoreCase("death")) {
                if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("search")) {
                        if (args.length >= 3) {
                            Object target_ = this.tools.main.utility.getPlayer(player, args[2].toLowerCase());
                            if (target_ instanceof OfflinePlayer) {
                                OfflinePlayer target = (OfflinePlayer) target_;
                                this.tools.main.deathLogger.getDeathsByPlayer(player, target);
                            }
                        } else {
                            player.sendMessage(this.tools.main.prefix + ChatColor.RED + "Please specify a player to search for deaths!");
                        }
                    }
                    if (args[1].equalsIgnoreCase("load")) {
                        if (args.length >= 3) {
                            this.tools.main.deathLogger.generateInventoryFromDeath(player, args[2]);
                        } else {
                            player.sendMessage(this.tools.main.prefix + ChatColor.RED + "Please specify a death ID to search!");
                        }
                    }
                } else {
                    player.sendMessage(this.tools.main.prefix + ChatColor.RED + "Please specify if you want to search or load a death!");
                }
            }
        } else {
            player.sendMessage(this.tools.main.prefix + ChatColor.RED + "You do not have permission to use this command!");
        }
        return true;
    }
}