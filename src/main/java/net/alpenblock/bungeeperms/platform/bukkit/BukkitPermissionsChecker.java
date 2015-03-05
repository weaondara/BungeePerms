package net.alpenblock.bungeeperms.platform.bukkit;

import lombok.AllArgsConstructor;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.ChatColor;
import net.alpenblock.bungeeperms.Color;
import net.alpenblock.bungeeperms.PermissionsChecker;
import net.alpenblock.bungeeperms.User;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class BukkitPermissionsChecker extends PermissionsChecker
{
    private final BukkitConfig config;
    
//without message
    /**
     * Checks if a user (no console) has a specific permission (globally).
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPerm(CommandSender sender, String permission)
    {
        if (sender instanceof Player)
        {
            return (config.isUseUUIDs() ? pm().getUser(((Player) sender).getUniqueId()) : pm().getUser(sender.getName())).hasPerm(permission);
        }
        return false;
    }

    /**
     * Checks if a user (or console) has a specific permission (globally). If sender is console this function return true.
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPermOrConsole(CommandSender sender, String permission)
    {
        if (sender instanceof Player)
        {
            return (config.isUseUUIDs() ? pm().getUser(((Player) sender).getUniqueId()) : pm().getUser(sender.getName())).hasPerm(permission);
        }
        else if (sender instanceof ConsoleCommandSender)
        {
            return true;
        }
        return false;
    }

    /**
     * Checks if a user (no console) has a specific permission on the current server.
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPermOnServer(CommandSender sender, String permission)
    {
        if (sender instanceof Player)
        {
            User user = config.isUseUUIDs() ? pm().getUser(((Player) sender).getUniqueId()) : pm().getUser(sender.getName());
            if (((Player) sender).getServer() == null)
            {
                return user.hasPerm(permission);
            }
            String server = ((BukkitConfig)BungeePerms.getInstance().getConfig()).getServername();
            return user.hasPermOnServer(permission, server);
        }
        return false;
    }

    /**
     * Checks if a user (or console) has a specific permission on the current server.
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPermOrConsoleOnServer(CommandSender sender, String permission)
    {
        if (sender instanceof Player)
        {
            User user = config.isUseUUIDs() ? pm().getUser(((Player) sender).getUniqueId()) : pm().getUser(sender.getName());
            if (((Player) sender).getServer() == null)
            {
                return user.hasPerm(permission);
            }
            String server = ((BukkitConfig)BungeePerms.getInstance().getConfig()).getServername();
            return user.hasPermOnServer(permission, server);
        }
        else if (sender instanceof ConsoleCommandSender)
        {
            return true;
        }
        return false;
    }

    /**
     * Checks if a user (no console) has a specific permission on the current server and in the current world.
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPermOnServerInWorld(CommandSender sender, String permission)
    {
        if (sender instanceof Player)
        {
            User user = config.isUseUUIDs() ? pm().getUser(((Player) sender).getUniqueId()) : pm().getUser(sender.getName());

            //per server
            if (((Player) sender).getServer() == null)
            {
                return user.hasPerm(permission);
            }

            //per server and world
            String server = ((BukkitConfig)BungeePerms.getInstance().getConfig()).getServername();
            String world = BukkitPlugin.getInstance().getListener().getPlayerWorlds().get(sender.getName());
            if (world == null)
            {
                return user.hasPermOnServer(permission, server);
            }

            return user.hasPermOnServerInWorld(permission, server, world);
        }
        return false;
    }

    /**
     * Checks if a user (or console) has a specific permission on the current server and in the current world.
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPermOrConsoleOnServerInWorld(CommandSender sender, String permission)
    {
        if (sender instanceof Player)
        {
            User user = config.isUseUUIDs() ? pm().getUser(((Player) sender).getUniqueId()) : pm().getUser(sender.getName());
            if (((Player) sender).getServer() == null)
            {
                return user.hasPerm(permission);
            }

            //per server and world
            String server = ((BukkitConfig)BungeePerms.getInstance().getConfig()).getServername();
            String world = BukkitPlugin.getInstance().getListener().getPlayerWorlds().get(sender.getName());
            if (world == null)
            {
                return user.hasPermOnServer(permission, server);
            }

            return user.hasPermOnServerInWorld(permission, server, world);
        }
        else if (sender instanceof ConsoleCommandSender)
        {
            return true;
        }
        return false;
    }

//with message
    /**
     * Checks if a user (no console) has a specific permission (globally).
     *
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
    public boolean has(CommandSender sender, String perm, boolean msg)
    {
        if (sender instanceof Player)
        {
            boolean isperm = (hasPerm(sender, perm));
            if (!isperm && msg)
            {
                sender.sendMessage(Color.Error + "You don't have permission to do that!" + ChatColor.RESET);
            }
            return isperm;
        }
        else
        {
            sender.sendMessage(Color.Error + "You don't have permission to do that!" + ChatColor.RESET);
            return false;
        }
    }

    /**
     * Checks if a user (or console) has a specific permission (globally).
     *
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
    public boolean hasOrConsole(CommandSender sender, String perm, boolean msg)
    {
        boolean isperm = (hasPerm(sender, perm) | (sender instanceof ConsoleCommandSender));
        if (!isperm && msg)
        {
            sender.sendMessage(Color.Error + "You don't have permission to do that!" + ChatColor.RESET);
        }
        return isperm;
    }

    /**
     * Checks if a user (no console) has a specific permission on the current server.
     *
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
    public boolean hasOnServer(CommandSender sender, String perm, boolean msg)
    {
        if (sender instanceof Player)
        {
            boolean isperm = hasPermOnServer(sender, perm);
            if (!isperm && msg)
            {
                sender.sendMessage(Color.Error + "You don't have permission to do that!" + ChatColor.RESET);
            }
            return isperm;
        }
        else
        {
            sender.sendMessage(Color.Error + "You don't have permission to do that!" + ChatColor.RESET);
            return false;
        }
    }

    /**
     * Checks if a user (or console) has a specific permission on the current server.
     *
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
    public boolean hasOrConsoleOnServer(CommandSender sender, String perm, boolean msg)
    {
        boolean isperm = (hasPermOnServer(sender, perm) | (sender instanceof ConsoleCommandSender));
        if (!isperm && msg)
        {
            sender.sendMessage(Color.Error + "You don't have permission to do that!" + ChatColor.RESET);
        }
        return isperm;
    }
    
    /**
     * Checks if a user (no console) has a specific permission on the current server and in the current world.
     *
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
    public boolean hasOnServerInWorld(CommandSender sender, String perm, boolean msg)
    {
        if (sender instanceof Player)
        {
            boolean isperm = hasPermOnServerInWorld(sender, perm);
            if (!isperm && msg)
            {
                sender.sendMessage(Color.Error + "You don't have permission to do that!" + ChatColor.RESET);
            }
            return isperm;
        }
        else
        {
            sender.sendMessage(Color.Error + "You don't have permission to do that!" + ChatColor.RESET);
            return false;
        }
    }

    /**
     * Checks if a user (or console) has a specific permission on the current server and in the current world.
     *
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
    public boolean hasOrConsoleOnServerInWorld(CommandSender sender, String perm, boolean msg)
    {
        boolean isperm = (hasPermOnServerInWorld(sender, perm) | (sender instanceof ConsoleCommandSender));
        if (!isperm && msg)
        {
            sender.sendMessage(Color.Error + "You don't have permission to do that!" + ChatColor.RESET);
        }
        return isperm;
    }
}