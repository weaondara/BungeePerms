package net.alpenblock.bungeeperms.platform.bungee;

import lombok.AllArgsConstructor;
import net.alpenblock.bungeeperms.Lang;
import net.alpenblock.bungeeperms.PermissionsChecker;
import net.alpenblock.bungeeperms.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@AllArgsConstructor
public class BungeePermissionsChecker extends PermissionsChecker
{

    private final BungeeConfig config;

//with messageout
    /**
     * Checks if a user (no console) has a specific permission (globally).
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPerm(CommandSender sender, String permission)
    {
        if (sender instanceof ProxiedPlayer)
        {
            return (config.isUseUUIDs() ? pm().getUser(((ProxiedPlayer) sender).getUniqueId()) : pm().getUser(sender.getName())).hasPerm(permission);
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
        if (sender instanceof ProxiedPlayer)
        {
            return (config.isUseUUIDs() ? pm().getUser(((ProxiedPlayer) sender).getUniqueId()) : pm().getUser(sender.getName())).hasPerm(permission);
        }
        else if (new BungeeSender(sender).isConsole())
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
        if (sender instanceof ProxiedPlayer)
        {
            User user = config.isUseUUIDs() ? pm().getUser(((ProxiedPlayer) sender).getUniqueId()) : pm().getUser(sender.getName());
            if (((ProxiedPlayer) sender).getServer() == null)
            {
                return user.hasPerm(permission);
            }
            return user.hasPermOnServer(permission, ((ProxiedPlayer) sender).getServer().getInfo().getName());
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
        if (sender instanceof ProxiedPlayer)
        {
            User user = config.isUseUUIDs() ? pm().getUser(((ProxiedPlayer) sender).getUniqueId()) : pm().getUser(sender.getName());
            if (((ProxiedPlayer) sender).getServer() == null)
            {
                return user.hasPerm(permission);
            }
            return user.hasPermOnServer(permission, ((ProxiedPlayer) sender).getServer().getInfo().getName());
        }
        else if (new BungeeSender(sender).isConsole())
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
        if (sender instanceof ProxiedPlayer)
        {
            User user = config.isUseUUIDs() ? pm().getUser(((ProxiedPlayer) sender).getUniqueId()) : pm().getUser(sender.getName());

            //per server
            if (((ProxiedPlayer) sender).getServer() == null)
            {
                return user.hasPerm(permission);
            }

            //per server and world
            String world = BungeePlugin.getInstance().getListener().getPlayerWorlds().get(sender.getName());
            if (world == null)
            {
                return user.hasPermOnServer(permission, ((ProxiedPlayer) sender).getServer().getInfo().getName());
            }

            return user.hasPermOnServerInWorld(permission, ((ProxiedPlayer) sender).getServer().getInfo().getName(), world);
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
        if (sender instanceof ProxiedPlayer)
        {
            User user = config.isUseUUIDs() ? pm().getUser(((ProxiedPlayer) sender).getUniqueId()) : pm().getUser(sender.getName());
            if (((ProxiedPlayer) sender).getServer() == null)
            {
                return user.hasPerm(permission);
            }

            //per server and world
            String world = BungeePlugin.getInstance().getListener().getPlayerWorlds().get(sender.getName());
            if (world == null)
            {
                return user.hasPermOnServer(permission, ((ProxiedPlayer) sender).getServer().getInfo().getName());
            }

            return user.hasPermOnServerInWorld(permission, ((ProxiedPlayer) sender).getServer().getInfo().getName(), world);
        }
        else if (new BungeeSender(sender).isConsole())
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
        if (sender instanceof ProxiedPlayer)
        {
            boolean isperm = (hasPerm(sender, perm));
            if (!isperm && msg)
            {
                sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
            }
            return isperm;
        }
        else
        {
            sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
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
        boolean isperm = hasPerm(sender, perm) || new BungeeSender(sender).isConsole();
        if (!isperm && msg)
        {
            sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
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
        if (sender instanceof ProxiedPlayer)
        {
            boolean isperm = hasPermOnServer(sender, perm);
            if (!isperm && msg)
            {
                sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
            }
            return isperm;
        }
        else
        {
            sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
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
        boolean isperm = hasPermOnServer(sender, perm) || new BungeeSender(sender).isConsole();
        if (!isperm && msg)
        {
            sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
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
        if (sender instanceof ProxiedPlayer)
        {
            boolean isperm = hasPermOnServerInWorld(sender, perm);
            if (!isperm && msg)
            {
                sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
            }
            return isperm;
        }
        else
        {
            sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
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
        boolean isperm = hasPermOnServerInWorld(sender, perm) || new BungeeSender(sender).isConsole();
        if (!isperm && msg)
        {
            sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
        }
        return isperm;
    }
}
