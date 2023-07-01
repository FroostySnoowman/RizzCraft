package rizzcraft.net.rizzcraft.Shops;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor {
    public final ShopHandler plugin;

    public CommandManager(ShopHandler plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("item") && sender instanceof Player player) {
            player.sendMessage(this.plugin.main.prefix + ChatColor.YELLOW + "You are holding: " + player.getInventory().getItemInMainHand().getType());
            return true;
        } else {
            if (label.equalsIgnoreCase("shop") && sender instanceof Player player && sender.hasPermission("rc.shops.*")) {
                if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                    player.sendMessage(ChatColor.GREEN + "======== RizzCraft Shop Help ========");
                    player.sendMessage(ChatColor.GREEN + "/shop " + ChatColor.AQUA + "create" + ChatColor.GRAY + " - Creates a new shop");
                    player.sendMessage(ChatColor.GREEN + "/shop " + ChatColor.AQUA + "delete" + ChatColor.GRAY + " - Deletes your own shop");
                    player.sendMessage(ChatColor.GREEN + "/shop " + ChatColor.AQUA + "name <name>" + ChatColor.GRAY + " - Gives your shop a name (without spaces)");
                    player.sendMessage(ChatColor.GREEN + "/shop " + ChatColor.AQUA + "info" + ChatColor.GRAY + " - Gets info on a given shop");
                    player.sendMessage(ChatColor.GREEN + "/shop " + ChatColor.AQUA + "transfer <player>" + ChatColor.GRAY + " - Transfers a shop to another player");
                    player.sendMessage(ChatColor.GREEN + "/shop " + ChatColor.AQUA + "search <item>" + ChatColor.GRAY + " - Locates a shop selling a certain item");
                    player.sendMessage(ChatColor.GREEN + "/shop " + ChatColor.AQUA + "collect" + ChatColor.GRAY + " - Collects payment from all your shops");
                    player.sendMessage(ChatColor.GREEN + "/shop " + ChatColor.AQUA + "toggle fireworks" + ChatColor.GRAY + " - Turns purchase fireworks on or off");
                    player.sendMessage(ChatColor.GREEN + "/shop " + ChatColor.AQUA + "transactions" + ChatColor.GRAY + " - View recent shop transactions");
                    player.sendMessage(ChatColor.GREEN + "/shop " + ChatColor.AQUA + "transactions" + ChatColor.GRAY + " - View recent shop transactions");
                    if (player.hasPermission("rc.mod")) {
                        player.sendMessage(ChatColor.GREEN + "/shop " + ChatColor.RED + "lock" + ChatColor.GRAY + " - " + ChatColor.DARK_RED + ChatColor.BOLD + "EMERGENCY" + ChatColor.RESET + ChatColor.GRAY + " - Locks all SHOPS");
                        player.sendMessage(ChatColor.GREEN + "/shop " + ChatColor.RED + "lockdown" + ChatColor.GRAY + " - " + ChatColor.DARK_RED + ChatColor.BOLD + "EMERGENCY" + ChatColor.RESET + ChatColor.GRAY + " - Locks all CHESTS");
                    }
                }

                if (args.length >= 1 && args[0].equalsIgnoreCase("create")) {
                    if (sender.hasPermission("rc.mod") && args.length > 1) {
                        this.plugin
                                .activatePointer(
                                        player,
                                        new String[]{"CREATE", args[1]},
                                        this.plugin.main.prefix
                                                + ChatColor.GOLD
                                                + "Right click the container you wish to make a shop, holding the item you wish to take as payment"
                                );
                    } else {
                        this.plugin
                                .activatePointer(
                                        player,
                                        new String[]{"CREATE"},
                                        this.plugin.main.prefix
                                                + ChatColor.GOLD
                                                + "Right click the container you wish to make a shop, holding the item you wish to take as payment"
                                );
                    }
                }

                if (args.length == 1 && args[0].equalsIgnoreCase("delete")) {
                    this.plugin
                            .activatePointer(player, new String[]{"DELETE"}, this.plugin.main.prefix + ChatColor.GOLD + "Right click the shop you wish to delete");
                }

                if (args.length == 2 && args[0].equalsIgnoreCase("name")) {
                    this.plugin
                            .activatePointer(player, new String[]{"NAME", args[1]}, this.plugin.main.prefix + ChatColor.GOLD + "Right click the shop you wish to name");
                }

                if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
                    this.plugin.activatePointer(player, new String[]{"INFO"}, this.plugin.main.prefix + ChatColor.GOLD + "Right click the shop you want info for");
                }

                if (args.length == 2 && args[0].equalsIgnoreCase("transfer")) {
                    Object target = this.plugin.main.utility.getPlayer(player, args[1]);
                    if (target != null) {
                        OfflinePlayer target_ = (OfflinePlayer)target;
                        this.plugin
                                .activatePointer(
                                        player,
                                        new String[]{"TRANSFER", target_.getUniqueId().toString()},
                                        this.plugin.main.prefix + ChatColor.GOLD + "Right click the shop you wish to transfer to " + target_.getName()
                                );
                    }
                }

                if (args.length == 2 && (args[0].equalsIgnoreCase("search") || args[0].equalsIgnoreCase("find") || args[0].equalsIgnoreCase("locate"))) {
                    Material itm;
                    try {
                        itm = Material.valueOf(args[1].toString().toUpperCase());
                    } catch (Exception var8) {
                        player.sendMessage(this.plugin.main.prefix + ChatColor.YELLOW + "That item id doesn't exist");
                        return true;
                    }

                    this.plugin.findShopsSelling(itm, player);
                }

                if (args.length == 1 && args[0].toLowerCase().equals("collect")) {
                    this.plugin.collectAllPayments(player);
                }

                if (args.length == 2 && args[0].toLowerCase().equals("toggle") && args[1].toLowerCase().equals("fireworks")) {
                    this.plugin.toggleFireworks(player);
                }

                if (args.length == 1
                        && (args[0].equalsIgnoreCase("transactions") || args[0].toLowerCase().equals("transaction") || args[0].toLowerCase().equals("recent"))) {
                    this.plugin
                            .activatePointer(player, new String[]{"TRANSACTION"}, this.plugin.main.prefix + ChatColor.GOLD + "Right click the shop you wish to look at");
                }

                if (args.length == 1 && args[0].equalsIgnoreCase("lock")) {
                    if (player.hasPermission("rc.mod")) {
                        this.plugin.lockedShops = !this.plugin.lockedShops;
                        this.plugin
                                .main
                                .utility
                                .alertMods(
                                        this.plugin.main.prefix
                                                + ChatColor.RED
                                                + player.getName()
                                                + " changed global shop lock value to: "
                                                + ChatColor.YELLOW
                                                + this.plugin.lockedShops
                                );
                    } else {
                        player.sendMessage(this.plugin.main.prefix + ChatColor.RED + "You do not have permission to use this command!");
                    }
                }

                if (args.length == 1 && args[0].equalsIgnoreCase("lockdown")) {
                    if (player.hasPermission("rc.mod")) {
                        this.plugin.lockedChests = !this.plugin.lockedChests;
                        this.plugin
                                .main
                                .utility
                                .alertMods(
                                        this.plugin.main.prefix
                                                + ChatColor.RED
                                                + player.getName()
                                                + " changed global chest lock value to: "
                                                + ChatColor.YELLOW
                                                + this.plugin.lockedChests
                                );
                    } else {
                        player.sendMessage(this.plugin.main.prefix + ChatColor.RED + "You do not have permission to use this command!");
                    }
                }
            }
            return true;
        }
    }
}