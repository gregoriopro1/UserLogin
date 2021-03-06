package com.elchologamer.userlogin.commands;

import com.elchologamer.pluginapi.util.command.SpigotCommand;
import com.elchologamer.userlogin.UserLogin;
import com.elchologamer.userlogin.api.UserLoginAPI;
import com.elchologamer.userlogin.api.enums.DestinationType;
import com.elchologamer.userlogin.api.enums.LoginType;
import com.elchologamer.userlogin.api.event.PlayerLoginEvent;
import com.elchologamer.userlogin.util.Path;
import com.elchologamer.userlogin.util.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegisterCommand extends SpigotCommand {

    private final UserLogin plugin;

    public RegisterCommand(UserLogin plugin) {
        super("register");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            Utils.sendMessage(Path.PLAYER_ONLY, sender);
            return true;
        }

        // Check if player is not already logged in
        Player player = (Player) sender;
        if (UserLoginAPI.isLoggedIn(player)) {
            Utils.sendMessage(Path.ALREADY_LOGGED_IN, player);
            return true;
        }

        // Check if player is not already registered
        if (UserLoginAPI.isRegistered(player)) {
            Utils.sendMessage(Path.ALREADY_REGISTERED, player);
            return true;
        }

        // Check usage
        if (args.length != 2) return false;

        // Check if password is over the minimum amount of characters
        String password = args[0];
        int minChars = plugin.getConfig().getInt("password.minCharacters");
        if (password.length() < minChars) {
            Utils.sendMessage(Path.SHORT_PASSWORD, player, new String[]{"chars"}, new String[]{Integer.toString(minChars)});
            return true;
        }

        // Check if both passwords match
        if (!password.equals(args[1])) {
            Utils.sendMessage(Path.DIFFERENT_PASSWORDS, player);
            return true;
        }

        // Call register event
        PlayerLoginEvent event = new PlayerLoginEvent(player, LoginType.REGISTER);

        if (Utils.normalMode() && !Utils.getConfig().getBoolean("teleports.toSpawn"))
            event.setDestinationLoc(null);

        UserLogin.getPlugin().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled())
            return true;

        // Player gets registered
        String uuid = player.getUniqueId().toString();

        Utils.changeLoggedIn(player, true);
        Utils.cancelTimeout(player);

        // Encrypt password if enabled
        if (plugin.getConfig().getBoolean("password.encrypt")) password = Utils.encrypt(password);

        if (!Utils.sqlMode()) {
            plugin.getPlayerData().get().set(uuid + ".password", password);

            // Set name and save file
            Utils.updateName(player);
            plugin.getPlayerData().save();
        } else
            UserLogin.getPlugin().getSQL().getData().put(UUID.fromString(uuid), password);

        // Send message, cancel timeout, and teleport to spawn if enabled
        if (event.getMessage() != null)
            player.sendMessage(event.getMessage());

        if (event.getDestinationType() == DestinationType.NORMAL) {
            // Send to spawn if enabled
            if (event.getDestinationLoc() != null)
                player.teleport(event.getDestinationLoc());

            // Announce join message to other players
            Utils.joinAnnounce(player, event.getAnnouncement());
            return true;
        }

        // Connect player to spawn server
        Utils.sendToServer(player, "");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
