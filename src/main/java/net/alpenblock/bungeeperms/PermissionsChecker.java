package net.alpenblock.bungeeperms;

import lombok.AllArgsConstructor;
import net.alpenblock.bungeeperms.platform.Sender;

@AllArgsConstructor
public class PermissionsChecker
{

    private final BPConfig config;

//withput message
    /**
     * Checks if a user (no console) has a specific permission (globally).
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPerm(String sender, String permission)
    {
        return hasPermOnServerInWorld(sender, permission, null, null);
    }

    /**
     * Checks if a user (no console) has a specific permission on the given server.
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @param server the server for additional permissions
     * @return the result of the permission check
     */
    public boolean hasPermOnServer(String sender, String permission, String server)
    {
        return hasPermOnServerInWorld(sender, permission, server, null);
    }

    /**
     * Checks if a user (no console) has a specific permission on the given server and in the given world.
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @param server the server for additional permissions
     * @param world the world for additional permissions
     * @return the result of the permission check
     */
    public boolean hasPermOnServerInWorld(String sender, String permission, String server, String world)
    {
        User u = pm().getUser(sender);
        if (u == null)
        {
            return false;
        }

        return u.hasPerm(permission, server, world);
    }

//with wrapped command senders
    /**
     * Checks if a user (no console) has a specific permission (globally).
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPerm(Sender sender, String permission)
    {
        if (sender.isConsole())
        {
            return false;
        }

        User u = config.isUseUUIDs() ? pm().getUser(sender.getUUID()) : pm().getUser(sender.getName());
        if (u == null)
        {
            return false;
        }
        return u.hasPerm(sender, permission, null, null);
    }

    /**
     * Checks if a user (or console) has a specific permission (globally). If sender is console this function return true.
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPermOrConsole(Sender sender, String permission)
    {
        return sender.isConsole() || hasPerm(sender, permission);
    }

    /**
     * Checks if a user (no console) has a specific permission. Server is fetched automatically.
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPermOnServer(Sender sender, String permission)
    {
        if (sender.isConsole())
        {
            return false;
        }

        User u = config.isUseUUIDs() ? pm().getUser(sender.getUUID()) : pm().getUser(sender.getName());
        if (u == null)
        {
            return false;
        }

        return u.hasPerm(sender, permission, sender.getServer(), null);
    }

    /**
     * Checks if a user (or console) has a specific permission. Server is fetched automatically.
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPermOrConsoleOnServer(Sender sender, String permission)
    {
        return sender.isConsole() || hasPermOnServer(sender, permission);
    }

    /**
     * Checks if a user (no console) has a specific permission. Server and world are fetched automatically.
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPermOnServerInWorld(Sender sender, String permission)
    {
        if (sender.isConsole())
        {
            return false;
        }

        User u = config.isUseUUIDs() ? pm().getUser(sender.getUUID()) : pm().getUser(sender.getName());
        if (u == null)
        {
            return false;
        }

        return u.hasPerm(sender, permission, sender.getServer(), sender.getWorld());
    }

    /**
     * Checks if a user (or console) has a specific permission. Server and world are fetched automatically.
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPermOrConsoleOnServerInWorld(Sender sender, String permission)
    {
        return sender.isConsole() || hasPermOnServerInWorld(sender, permission);
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
    public boolean has(Sender sender, String perm, boolean msg)
    {
        boolean isperm = hasPerm(sender, perm);
        if (!isperm && msg)
        {
            sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
        }
        return isperm;
    }

    /**
     * Checks if a user (or console) has a specific permission (globally).
     *
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
    public boolean hasOrConsole(Sender sender, String perm, boolean msg)
    {
        boolean isperm = hasPermOrConsole(sender, perm);
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
    public boolean hasOnServer(Sender sender, String perm, boolean msg)
    {
        boolean isperm = hasPermOnServer(sender, perm);
        if (!isperm && msg)
        {
            sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
        }
        return isperm;
    }

    /**
     * Checks if a user (or console) has a specific permission on the current server.
     *
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
    public boolean hasOrConsoleOnServer(Sender sender, String perm, boolean msg)
    {
        boolean isperm = hasPermOrConsoleOnServer(sender, perm);
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
    public boolean hasOnServerInWorld(Sender sender, String perm, boolean msg)
    {
        boolean isperm = hasPermOnServerInWorld(sender, perm);
        if (!isperm && msg)
        {
            sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
        }
        return isperm;
    }

    /**
     * Checks if a user (or console) has a specific permission on the current server and in the current world.
     *
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
    public boolean hasOrConsoleOnServerInWorld(Sender sender, String perm, boolean msg)
    {
        boolean isperm = hasPermOrConsoleOnServerInWorld(sender, perm);
        if (!isperm && msg)
        {
            sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
        }
        return isperm;
    }

    protected PermissionsManager pm()
    {
        return BungeePerms.getInstance().getPermissionsManager();
    }
}
