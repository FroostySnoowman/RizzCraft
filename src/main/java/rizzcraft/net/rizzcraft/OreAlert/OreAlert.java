package rizzcraft.net.rizzcraft.OreAlert;

import rizzcraft.net.rizzcraft.Main;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class OreAlert {
    private final Main main;
    private final ArrayList<Block> ignoreList;

    public OreAlert(Main main) {
        this.main = main;
        this.ignoreList = new ArrayList();
    }

    public void handleEvent(BlockBreakEvent event) {
        if (Objects.requireNonNull(this.main.config.getConfig().getList("OreAlert.Blocks")).contains(event.getBlock().getType().toString())) {
            if (!this.ignoreList.contains(event.getBlock())) {
                ArrayList<Block> nearbyBlocks = this.main.utility.getBlocksInRadiusOf(event.getBlock().getLocation(), 5, event.getBlock().getType());

                for(Block block : nearbyBlocks) {
                    if (!this.ignoreList.contains(block)) {
                        this.ignoreList.add(block);
                    }
                }

                TextComponent alert = new TextComponent(
                        this.main.prefix + ChatColor.GRAY + event.getPlayer().getName() + " found " + nearbyBlocks.size() + " " + event.getBlock().getType()
                );
                TextComponent alertTP = new TextComponent(ChatColor.DARK_GRAY + " [Teleport]");
                alertTP.setClickEvent(
                        new ClickEvent(
                                Action.RUN_COMMAND,
                                "/teleport "
                                        + event.getBlock().getLocation().getBlockX()
                                        + " "
                                        + event.getBlock().getLocation().getBlockY()
                                        + " "
                                        + event.getBlock().getLocation().getBlockZ()
                        )
                );

                for(Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("rc.mod")) {
                        p.spigot().sendMessage(new BaseComponent[]{alert, alertTP});
                    }
                }
            }

            if (this.ignoreList.contains(event.getBlock())) {
                this.ignoreList.remove(this.ignoreList.indexOf(event.getBlock()));
            }

            this.addStat(event.getPlayer(), event.getBlock());
        }
    }

    public void addStat(Player player, Block block) {
        try {
            try (Connection con = this.main.sql.connectionPool.getConnection()) {
                PreparedStatement pstmt = con.prepareStatement("INSERT INTO oreAlert (PlayerUUID, BlockType) VALUES (?,?)");
                pstmt.setString(1, player.getUniqueId().toString());
                pstmt.setString(2, block.getType().toString());
                pstmt.executeUpdate();
                pstmt.close();
                con.close();
            }
        } catch (SQLException var8) {
            throw new ArithmeticException("\u001B[31maddStat() failed to insert ore break event record: " + var8.getMessage() + "\u001B[0m");
        }
    }
}