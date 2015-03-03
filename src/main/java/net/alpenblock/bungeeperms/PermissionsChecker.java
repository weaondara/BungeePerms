package net.alpenblock.bungeeperms;

import net.alpenblock.bungeeperms.platform.Sender;

public class PermissionsChecker
{

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
        if (!sender.equalsIgnoreCase("CONSOLE"))
        {
            User u = pm().getUser(sender);
            if (u == null)
            {
                return false;
            }
            return u.hasPerm(permission);
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
    public boolean hasPermOrConsole(String sender, String permission)
    {
        if (sender.equalsIgnoreCase("CONSOLE"))
        {
            return true;
        }
        else
        {
            User u = pm().getUser(sender);
            if (u == null)
            {
                return false;
            }
            return u.hasPerm(permission);
        }
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
        if (!sender.equalsIgnoreCase("CONSOLE"))
        {
            User u = pm().getUser(sender);
            if (u == null)
            {
                return false;
            }

            if (server == null)
            {
                return hasPerm(sender, permission);
            }

            return u.hasPermOnServer(permission, server);
        }
        return false;
    }

    /**
     * Checks if a user (or console) has a specific permission on the given server.
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @param server the server for additional permissions
     * @return the result of the permission check
     */
    public boolean hasPermOrConsoleOnServer(String sender, String permission, String server)
    {
        if (sender.equalsIgnoreCase("CONSOLE"))
        {
            return true;
        }
        else
        {
            User u = pm().getUser(sender);
            if (u == null)
            {
                return false;
            }

            if (server == null)
            {
                return hasPerm(sender, permission);
            }

            return u.hasPermOnServer(permission, server);
        }
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
        if (!sender.equalsIgnoreCase("CONSOLE"))
        {
            User u = pm().getUser(sender);
            if (u == null)
            {
                return false;
            }

            if (server == null)
            {
                return hasPerm(sender, permission);
            }

            if (world == null)
            {
                return hasPermOnServer(sender, permission, server);
            }

            return u.hasPermOnServerInWorld(permission, server, world);
        }
        return false;
    }

    /**
     * Checks if a user (or console) has a specific permission on the given server and in the given world.
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @param server the server for additional permissions
     * @param world the world for additional permissions
     * @return the result of the permission check
     */
    public boolean hasPermOrConsoleOnServerInWorld(String sender, String permission, String server, String world)
    {
        if (sender.equalsIgnoreCase("CONSOLE"))
        {
            return true;
        }
        else
        {
            User u = pm().getUser(sender);
            if (u == null)
            {
                return false;
            }

            if (server == null)
            {
                return hasPerm(sender, permission);
            }

            if (world == null)
            {
                return hasPermOnServer(sender, permission, server);
            }

            return u.hasPermOnServerInWorld(permission, server, world);
        }
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
        if (sender.isPlayer())
        {
            boolean isperm = (hasPerm(sender.getName(), perm));
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
    public boolean hasOrConsole(Sender sender, String perm, boolean msg)
    {
        boolean isperm = (hasPerm(sender.getName(), perm) || sender.isConsole());
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
    public boolean hasOnServer(Sender sender, String perm, boolean msg)
    {
        if (sender.isPlayer())
        {
            boolean isperm = hasPermOnServer(sender.getName(), perm, sender.getServer());
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
    public boolean hasOrConsoleOnServer(Sender sender, String perm, boolean msg)
    {
        boolean isperm = (hasPermOnServer(sender.getName(), perm, sender.getServer()) || sender.isConsole());
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
    public boolean hasOnServerInWorld(Sender sender, String perm, boolean msg)
    {
        if (sender.isPlayer())
        {
            boolean isperm = hasPermOnServerInWorld(sender.getName(), perm, sender.getServer(), sender.getWorld());
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
    public boolean hasOrConsoleOnServerInWorld(Sender sender, String perm, boolean msg)
    {
        boolean isperm = (hasPermOnServerInWorld(sender.getName(), perm, sender.getServer(), sender.getWorld()) || sender.isConsole());
        if (!isperm && msg)
        {
            sender.sendMessage(Color.Error + "You don't have permission to do that!" + ChatColor.RESET);
        }
        return isperm;
    }

    protected PermissionsManager pm()
    {
        return BungeePerms.getInstance().getPermissionsManager();
    }
}
