package rizzcraft.net.rizzcraft.Tools;

import rizzcraft.net.rizzcraft.Main;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.bukkit.command.PluginCommand;

public class Tools {
    public final Main main;
    public final CommandManager commandManager;
    public final TabCompleteManager tabCompleteManager;
    public final Whois whois;
    public final Warn warn;
    public final Admin admin;
    public final List<String> admins;

    public Tools(Main main) {
        this.main = main;
        this.commandManager = new CommandManager(this);
        this.tabCompleteManager = new TabCompleteManager();
        this.whois = new Whois(this);
        this.warn = new Warn(this);
        this.admin = new Admin(this);
        this.admins = Arrays.asList(this.main.config.getConfig().getList("Admins").toArray(new String[0]));
        this.initialize();
    }

    public void initialize() {
        ((PluginCommand)Objects.requireNonNull(this.main.getCommand("rc"))).setExecutor(this.commandManager);
        ((PluginCommand)Objects.requireNonNull(this.main.getCommand("rc"))).setTabCompleter(this.tabCompleteManager);
    }
}