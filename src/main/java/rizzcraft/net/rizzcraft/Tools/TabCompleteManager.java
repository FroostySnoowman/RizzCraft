package rizzcraft.net.rizzcraft.Tools;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class TabCompleteManager implements TabCompleter {
    final List<String> arguments = new ArrayList<>();

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (this.arguments.isEmpty()) {
            this.arguments.add("ping");
            this.arguments.add("whois");
            this.arguments.add("a");
            this.arguments.add("help");
            this.arguments.add("death");
            this.arguments.add("warn");
        }

        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for(String a : this.arguments) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(a);
                }
            }

            return result;
        } else {
            return null;
        }
    }
}