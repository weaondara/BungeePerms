/* 
 * Copyright (C) 2020 wea_ondara
 *
 * BungeePerms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BungeePerms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        User u = pm().getUser(sender);
        if (u == null)
        {
            return false;
        }
        return u.hasPerm(permission, null, null);
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
        User u = pm().getUser(sender);
        if (u == null)
        {
            return false;
        }

        if (server == null)
        {
            return hasPerm(sender, permission);
        }

        return u.hasPerm(permission, server, null);
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

        if (server == null)
        {
            return hasPerm(sender, permission);
        }

        if (world == null)
        {
            return hasPermOnServer(sender, permission, server);
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
        if (!sender.isConsole())
        {
            User u = pm().getUser(sender.getName());
            if (u == null)
            {
                return false;
            }
            return u.hasPerm(sender, permission, null, null);
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
    public boolean hasPermOrConsole(Sender sender, String permission)
    {
        if (sender.isConsole())
        {
            return true;
        }
        else
        {
            User u = pm().getUser(sender.getName());
            if (u == null)
            {
                return false;
            }
            return u.hasPerm(sender, permission, null, null);
        }
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
        User u = pm().getUser(sender.getName());
        if (u == null)
        {
            return false;
        }

        if (sender.getServer() == null)
        {
            return hasPerm(sender, permission);
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
        if (sender.isConsole())
        {
            return true;
        }
        else
        {
            User u = pm().getUser(sender.getName());
            if (u == null)
            {
                return false;
            }

            if (sender.getServer() == null)
            {
                return hasPerm(sender, permission);
            }

            return u.hasPerm(sender, permission, sender.getServer(), null);
        }
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
        User u = pm().getUser(sender.getName());
        if (u == null)
        {
            return false;
        }

        if (sender.getServer() == null)
        {
            return hasPerm(sender, permission);
        }

        if (sender.getWorld() == null)
        {
            return hasPermOnServer(sender, permission);
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
        if (sender.isConsole())
        {
            return true;
        }
        else
        {
            User u = pm().getUser(sender.getName());
            if (u == null)
            {
                return false;
            }

            if (sender.getServer() == null)
            {
                return hasPerm(sender, permission);
            }

            if (sender.getWorld() == null)
            {
                return hasPermOnServer(sender, permission);
            }

            return u.hasPerm(sender, permission, sender.getServer(), sender.getWorld());
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
            boolean isperm = (hasPerm(sender, perm));
            if (!isperm && msg)
            {
                sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
            }
            return isperm;
        }
        else
        {
            if (msg)
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
    public boolean hasOrConsole(Sender sender, String perm, boolean msg)
    {
        boolean isperm = (hasPerm(sender, perm) || sender.isConsole());
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
        if (sender.isPlayer())
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
            if (msg)
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
    public boolean hasOrConsoleOnServer(Sender sender, String perm, boolean msg)
    {
        boolean isperm = (hasPermOnServer(sender, perm) || sender.isConsole());
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
        if (sender.isPlayer())
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
            if (msg)
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
    public boolean hasOrConsoleOnServerInWorld(Sender sender, String perm, boolean msg)
    {
        boolean isperm = (hasPermOnServerInWorld(sender, perm) || sender.isConsole());
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
