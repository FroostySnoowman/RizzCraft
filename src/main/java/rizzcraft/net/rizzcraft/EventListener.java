package rizzcraft.net.rizzcraft;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Arrays;
import java.util.List;

public class EventListener extends Event implements Listener {
    private final Main main;

    public EventListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        this.main.oreAlert.handleEvent(event);
        this.main.shopHandler.handleBlockBreakEvent(event);
    }

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (this.main.tools.admin.activeAdminChat.contains(event.getPlayer())) {
            event.setCancelled(true);
            this.main
                    .tools
                    .main
                    .utility
                    .alertMods(ChatColor.DARK_RED + "[RC]" + ChatColor.RESET + " <" + event.getPlayer().getDisplayName() + "> " + ChatColor.RED + event.getMessage());
        }
    }

    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        if (event.getPlayer().isBanned()) {
            this.main.utility.alertMods(this.main.prefix + ChatColor.GRAY + event.getPlayer().getName() + ChatColor.GRAY + " tried to login but is banned.");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        int playtimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        int playtimeSeconds = playtimeTicks / 20;

        String playerName = player.getName();

        if (playtimeSeconds >= 7200 && !player.hasPermission("essentials.sethome.one")) {
            String command = "lp user " + playerName + " permission set essentials.sethome.one";
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            Bukkit.getScheduler().runTaskLater(this.main, () -> {
                player.sendMessage(this.main.prefix + ChatColor.YELLOW + "You've been awarded one sethome for your playtime!");
            }, 20L);
        }
        if (playtimeSeconds >= 28800 && !player.hasPermission("essentials.sethome.two")) {
            String command = "lp user " + playerName + " permission set essentials.sethome.two";
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            Bukkit.getScheduler().runTaskLater(this.main, () -> {
                player.sendMessage(this.main.prefix + ChatColor.YELLOW + "You've been awarded a second sethome for your playtime!");
            }, 20L);
        }
        if (playtimeSeconds >= 57600 && !player.hasPermission("essentials.sethome.three")) {
            String command = "lp user " + playerName + " permission set essentials.sethome.three";
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            Bukkit.getScheduler().runTaskLater(this.main, () -> {
                player.sendMessage(this.main.prefix + ChatColor.YELLOW + "You've been awarded a third sethome for your playtime!");
            }, 20L);
        }
        if (playtimeSeconds >= 57600 && !player.hasPermission("essentials.nick")) {
            String command1 = "lp user " + playerName + " permission set essentials.nick";
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command1);
            String command2 = "lp user " + playerName + " permission set essentials.nick.changecolors";
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command2);
            String command3 = "lp user " + playerName + " permission set essentials.nick.color";
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command3);
            Bukkit.getScheduler().runTaskLater(this.main, () -> {
                player.sendMessage(this.main.prefix + ChatColor.YELLOW + "You've been awarded nickname perks for your playtime!");
            }, 20L);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().substring(1);

        List<String> blockedCommands = Arrays.asList(this.main.config.getConfig().getList("BlockedCommands").toArray(new String[0]));

        if (!event.getPlayer().hasPermission("rc.mod")) {
            if (blockedCommands.contains(command.toLowerCase())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(this.main.prefix + ChatColor.RED + "You don't have permission to use this command.");
            }
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        this.main.deathMessages.handleEvent(event);
    }

    public HandlerList getHandlers() {
        return null;
    }
}