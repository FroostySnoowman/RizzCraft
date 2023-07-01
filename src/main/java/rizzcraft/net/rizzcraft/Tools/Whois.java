package rizzcraft.net.rizzcraft.Tools;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class Whois {
    private final Tools plugin;

    public Whois(Tools plugin) {
        this.plugin = plugin;
    }

    public void get(Player player, String[] args) {
        UUID playerID = this.plugin.main.utility.getPlayerUUID(args[1].toLowerCase());
        if (playerID == null) {
            player.sendMessage(this.plugin.main.prefix + "That player doesn't exist.");
        } else {
            OfflinePlayer whoisPlayer = Bukkit.getOfflinePlayer(playerID);
            Date firstOnline = new Date(whoisPlayer.getFirstPlayed());
            Date lastOnline = new Date(whoisPlayer.getLastPlayed());
            Location bedsp = whoisPlayer.getBedSpawnLocation();
            int statDamageDealt = whoisPlayer.getStatistic(Statistic.DAMAGE_DEALT);
            int statDamageTaken = whoisPlayer.getStatistic(Statistic.DAMAGE_TAKEN);
            boolean isOnline = whoisPlayer.isOnline();
            boolean isOP = whoisPlayer.isOp();
            boolean isBanned = whoisPlayer.isBanned();
            boolean isWL = whoisPlayer.isWhitelisted();
            if (bedsp == null) {
                bedsp = new Location(player.getLocation().getWorld(), 0.0, 0.0, 0.0);
            }

            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "Whois data for: " + ChatColor.RESET + whoisPlayer.getName());
            TextComponent copyUUID = new TextComponent(ChatColor.GRAY + playerID.toString());
            copyUUID.setClickEvent(new ClickEvent(Action.COPY_TO_CLIPBOARD, playerID.toString()));
            copyUUID.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text("Click to Copy")}));
            player.spigot().sendMessage(new BaseComponent[]{new TextComponent(ChatColor.GOLD + "UUID: "), copyUUID});
            player.sendMessage(ChatColor.GOLD + "OP: " + ChatColor.RESET + isOP);
            player.sendMessage(ChatColor.GOLD + "Banned: " + ChatColor.RESET + isBanned);
            player.sendMessage(ChatColor.GOLD + "Whitelisted: " + ChatColor.RESET + isWL);
            player.sendMessage(ChatColor.GOLD + "Online: " + ChatColor.RESET + isOnline);
            player.sendMessage(ChatColor.GOLD + "First Online: " + ChatColor.RESET + firstOnline);
            player.sendMessage(ChatColor.GOLD + "Last Online: " + ChatColor.RESET + lastOnline);
            TextComponent teleportBed = new TextComponent(ChatColor.GRAY + " [Teleport]");
            teleportBed.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/teleport " + bedsp.getBlockX() + " " + bedsp.getBlockY() + " " + bedsp.getBlockZ()));
            player.spigot()
                    .sendMessage(
                            new BaseComponent[]{
                                    new TextComponent(
                                                    ChatColor.GOLD
                                                    + "Bed: "
                                                    + ChatColor.RESET
                                                    + bedsp.getBlockX()
                                                    + ", "
                                                    + bedsp.getBlockY()
                                                    + ", "
                                                    + bedsp.getBlockZ()
                                    ),
                                    teleportBed
                            }
                    );
            player.sendMessage(ChatColor.GOLD + "Damage Dealt: " + ChatColor.RESET + statDamageDealt);
            player.sendMessage(ChatColor.GOLD + "Damage Taken: " + ChatColor.RESET + statDamageTaken);
            if (whoisPlayer.isOnline()) {
                Player whoisPlayerOnline = Bukkit.getPlayer(playerID);
                InetSocketAddress IPAddr = whoisPlayerOnline.getAddress();
                boolean isFly = whoisPlayerOnline.getAllowFlight();
                int clientViewDistance = whoisPlayerOnline.getClientViewDistance();
                String nick = whoisPlayerOnline.getCustomName();
                String displayName = whoisPlayerOnline.getDisplayName();
                float exp = whoisPlayerOnline.getExp();
                float expToLevel = (float)whoisPlayerOnline.getExpToLevel();
                int food = whoisPlayerOnline.getFoodLevel();
                double health = whoisPlayerOnline.getHealth();
                String killerName = "None";
                Player killer = whoisPlayerOnline.getKiller();
                int level = whoisPlayerOnline.getLevel();
                Location pos = whoisPlayerOnline.getLocation();
                if (nick == null) {
                    nick = "Not Set";
                }

                if (killer != null) {
                    killerName = killer.getName();
                }

                player.sendMessage(ChatColor.GOLD + "IP Addr: " + ChatColor.RESET + IPAddr);
                player.sendMessage(ChatColor.GOLD + "Fly: " + ChatColor.RESET + isFly);
                player.sendMessage(ChatColor.GOLD + "View Distance: " + ChatColor.RESET + clientViewDistance);
                player.sendMessage(ChatColor.GOLD + "Nick: " + ChatColor.RESET + nick);
                player.sendMessage(ChatColor.GOLD + "Display Name: " + ChatColor.RESET + displayName);
                player.sendMessage(ChatColor.GOLD + "EXP: " + ChatColor.RESET + exp + "/" + expToLevel + " (lvl " + level + ")");
                player.sendMessage(ChatColor.GOLD + "Food: " + ChatColor.RESET + food);
                player.sendMessage(ChatColor.GOLD + "Health: " + ChatColor.RESET + health);
                player.sendMessage(ChatColor.GOLD + "Killer: " + ChatColor.RESET + killerName);
                TextComponent teleportPos = new TextComponent(ChatColor.GRAY + " [Teleport]");
                teleportPos.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/teleport " + pos.getBlockX() + " " + pos.getBlockY() + " " + pos.getBlockZ()));
                player.spigot()
                        .sendMessage(
                                new BaseComponent[]{
                                        new TextComponent(
                                                        ChatColor.GOLD
                                                        + "Pos: "
                                                        + ChatColor.RESET
                                                        + pos.getBlockX()
                                                        + ", "
                                                        + pos.getBlockY()
                                                        + ", "
                                                        + pos.getBlockZ()
                                        ),
                                        teleportPos
                                }
                        );
            }
        }
    }
}