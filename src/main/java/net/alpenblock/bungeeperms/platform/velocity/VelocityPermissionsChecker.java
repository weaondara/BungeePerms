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
package net.alpenblock.bungeeperms.platform.velocity;

import lombok.AllArgsConstructor;
import net.alpenblock.bungeeperms.Lang;
import net.alpenblock.bungeeperms.PermissionsChecker;
import net.alpenblock.bungeeperms.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@AllArgsConstructor
public class VelocityPermissionsChecker extends PermissionsChecker
{

    private final VelocityConfig config;

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
            return (config.isUseUUIDs() ? pm().getUser(((ProxiedPlayer) sender).getUniqueId()) : pm().getUser(sender.getName())).hasPerm(permission, null, null);
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
            return (config.isUseUUIDs() ? pm().getUser(((ProxiedPlayer) sender).getUniqueId()) : pm().getUser(sender.getName())).hasPerm(permission, null, null);
        }
        else if (new VelocitySender(sender).isConsole())
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
                return user.hasPerm(permission, null, null);
            }
            return user.hasPerm(permission, ((ProxiedPlayer) sender).getServer().getInfo().getName(), null);
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
                return user.hasPerm(permission, null, null);
            }
            return user.hasPerm(permission, ((ProxiedPlayer) sender).getServer().getInfo().getName(), null);
        }
        else if (new VelocitySender(sender).isConsole())
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
                return user.hasPerm(permission, null, null);
            }

            //per server and world
            String world = VelocityPlugin.getInstance().getListener().getPlayerWorlds().get(sender.getName());
            if (world == null)
            {
                return user.hasPerm(permission, ((ProxiedPlayer) sender).getServer().getInfo().getName(), null);
            }

            return user.hasPerm(permission, ((ProxiedPlayer) sender).getServer().getInfo().getName(), world);
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
                return user.hasPerm(permission, null, null);
            }

            //per server and world
            String world = VelocityPlugin.getInstance().getListener().getPlayerWorlds().get(sender.getName());
            if (world == null)
            {
                return user.hasPerm(permission, ((ProxiedPlayer) sender).getServer().getInfo().getName(), null);
            }

            return user.hasPerm(permission, ((ProxiedPlayer) sender).getServer().getInfo().getName(), world);
        }
        else if (new VelocitySender(sender).isConsole())
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
        boolean isperm = hasPerm(sender, perm) || new VelocitySender(sender).isConsole();
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
        boolean isperm = hasPermOnServer(sender, perm) || new VelocitySender(sender).isConsole();
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
        boolean isperm = hasPermOnServerInWorld(sender, perm) || new VelocitySender(sender).isConsole();
        if (!isperm && msg)
        {
            sender.sendMessage(Lang.translate(Lang.MessageType.NO_PERM));
        }
        return isperm;
    }
}
