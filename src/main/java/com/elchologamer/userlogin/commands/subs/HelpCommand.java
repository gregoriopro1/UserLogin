package com.elchologamer.userlogin.commands.subs;

import com.elchologamer.pluginapi.util.command.SubCommand;
import com.elchologamer.userlogin.UserLogin;
import com.elchologamer.userlogin.util.Path;
import com.elchologamer.userlogin.util.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class HelpCommand extends SubCommand {

    public HelpCommand() {
        super("help", "/help");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) return false;

        FileConfiguration messages = UserLogin.getPlugin().getMessages();
        if(messages == null) return true;

        for (String str : messages.getStringList(Path.HELP)) {
            sender.sendMessage(Utils.color(str));
        }
        return true;
    }
}
