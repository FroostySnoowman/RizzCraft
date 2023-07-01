package rizzcraft.net.rizzcraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rizzcraft.net.rizzcraft.Death.DeathLogger;
import rizzcraft.net.rizzcraft.Death.Messages;
import rizzcraft.net.rizzcraft.OreAlert.OreAlert;
import rizzcraft.net.rizzcraft.SQL.SQL;
import rizzcraft.net.rizzcraft.Shops.ShopHandler;
import rizzcraft.net.rizzcraft.Tools.Tools;
import rizzcraft.net.rizzcraft.Welcomer.Welcomer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    public final String prefix = ChatColor.GREEN + "[RC] " + ChatColor.RESET;
    public DataManager config;
    public DataManager deathMessageData;
    public Utility utility;
    public Welcomer welcomer;
    public ShopHandler shopHandler;
    public Messages deathMessages;
    public DeathLogger deathLogger;
    public OreAlert oreAlert;
    public SQL sql;
    public Tools tools;
    public Logger logger;

    public void onEnable() {
        this.logger = this.getLogger();
        this.config = new DataManager(this, "config.yml");
        this.deathMessageData = new DataManager(this, "deathMessages.yml");
        this.utility = new Utility(this);
        this.tools = new Tools(this);
        this.welcomer = new Welcomer(this);
        this.shopHandler = new ShopHandler(this);
        this.oreAlert = new OreAlert(this);
        this.deathMessages = new Messages(this);
        this.deathLogger = new DeathLogger(this);
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
        this.sql = new SQL(this);
        this.sql.Connect();
        this.logger.log(Level.INFO, "\u001B[32mReady\u001B[0m");
    }

    public void onDisable() {
        if (this.sql != null)
            this.sql.Disconnect();
        this.logger.log(Level.INFO, "\u001B[31mShutdown\u001B[0m");
    }
}