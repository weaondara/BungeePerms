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

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import lombok.AllArgsConstructor;
import net.alpenblock.bungeeperms.Lang;
import net.alpenblock.bungeeperms.PermissionsChecker;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.proxy.ProxyConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

@AllArgsConstructor
public class VelocityPermissionsChecker extends PermissionsChecker
{

    private final ProxyConfig config;

//with messageout
    /**
     * Checks if a user (no console) has a specific permission (globally).
     *
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPerm(CommandSource sender, String permission)
    {
        if (sender instanceof Player)
        {
            return (config.isUseUUIDs() ? pm().getUser(((Player) sender).getUniqueId()) : pm().getUser(((Player) sender).getUsername())).hasPerm(permission, null, null);
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
    public boolean hasPermOrConsole(CommandSource sender, String permission)
    {
        if (sender instanceof Player)
        {
            return (config.isUseUUIDs() ? pm().getUser(((Player) sender).getUniqueId()) : pm().getUser(((Player) sender).getUsername())).hasPerm(permission, null, null);
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
    public boolean hasPermOnServer(CommandSource sender, String permission)
    {
        if (sender instanceof Player)
        {
            User user = config.isUseUUIDs() ? pm().getUser(((Player) sender).getUniqueId()) : pm().getUser(((Player) sender).getUsername());
            if (((Player) sender).getCurrentServer() == null)
            {
                return user.hasPerm(permission, null, null);
            }
            return user.hasPerm(permission, ((Player) sender).getCurrentServer().get().getServerInfo().getName(), null);
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
    public boolean hasPermOrConsoleOnServer(CommandSource sender, String permission)
    {
        if (sender instanceof Player)
        {
            User user = config.isUseUUIDs() ? pm().getUser(((Player) sender).getUniqueId()) : pm().getUser(((Player) sender).getUsername());
            if (((Player) sender).getCurrentServer() == null)
            {
                return user.hasPerm(permission, null, null);
            }
            return user.hasPerm(permission, ((Player) sender).getCurrentServer().get().getServerInfo().getName(), null);
        }
        else if (sender instanceof ConsoleCommandSource)
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
    public boolean hasPermOnServerInWorld(CommandSource sender, String permission)
    {
        if (sender instanceof Player)
        {
            User user = config.isUseUUIDs() ? pm().getUser(((Player) sender).getUniqueId()) : pm().getUser(((Player) sender).getUsername());

            //per server
            if (((Player) sender).getCurrentServer() == null)
            {
                return user.hasPerm(permission, null, null);
            }

            //per server and world
            String world = VelocityPlugin.getInstance().getListener().getPlayerWorlds().get(((Player) sender).getUsername());
            if (world == null)
            {
                return user.hasPerm(permission, ((Player) sender).getCurrentServer().get().getServerInfo().getName(), null);
            }

            return user.hasPerm(permission, ((Player) sender).getCurrentServer().get().getServerInfo().getName(), world);
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
    public boolean hasPermOrConsoleOnServerInWorld(CommandSource sender, String permission)
    {
        if (sender instanceof Player)
        {
            User user = config.isUseUUIDs() ? pm().getUser(((Player) sender).getUniqueId()) : pm().getUser(((Player) sender).getUsername());
            if (((Player) sender).getCurrentServer() == null)
            {
                return user.hasPerm(permission, null, null);
            }

            //per server and world
            String world = VelocityPlugin.getInstance().getListener().getPlayerWorlds().get(((Player) sender).getUsername());
            if (world == null)
            {
                return user.hasPerm(permission, ((Player) sender).getCurrentServer().get().getServerInfo().getName(), null);
            }

            return user.hasPerm(permission, ((Player) sender).getCurrentServer().get().getServerInfo().getName(), world);
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
    public boolean has(CommandSource sender, String perm, boolean msg)
    {
        if (sender instanceof Player)
        {
            boolean isperm = (hasPerm(sender, perm));
            if (!isperm && msg)
            {
                Component t = Component.text(Lang.translate(Lang.MessageType.NO_PERM));
                sender.sendMessage(t);
            }
            return isperm;
        }
        else
        {
            Component t = Component.text(Lang.translate(Lang.MessageType.NO_PERM));
            sender.sendMessage(t);
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
    public boolean hasOrConsole(CommandSource sender, String perm, boolean msg)
    {
        boolean isperm = hasPerm(sender, perm) || new VelocitySender(sender).isConsole();
        if (!isperm && msg)
        {
            Component t = Component.text(Lang.translate(Lang.MessageType.NO_PERM));
            sender.sendMessage(t);
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
    public boolean hasOnServer(CommandSource sender, String perm, boolean msg)
    {
        if (sender instanceof Player)
        {
            boolean isperm = hasPermOnServer(sender, perm);
            if (!isperm && msg)
            {
                Component t = Component.text(Lang.translate(Lang.MessageType.NO_PERM));
                sender.sendMessage(t);
            }
            return isperm;
        }
        else
        {
            Component t = Component.text(Lang.translate(Lang.MessageType.NO_PERM));
            sender.sendMessage(t);
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
    public boolean hasOrConsoleOnServer(CommandSource sender, String perm, boolean msg)
    {
        boolean isperm = hasPermOnServer(sender, perm) || new VelocitySender(sender).isConsole();
        if (!isperm && msg)
        {
            Component t = Component.text(Lang.translate(Lang.MessageType.NO_PERM));
            sender.sendMessage(t);
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
    public boolean hasOnServerInWorld(CommandSource sender, String perm, boolean msg)
    {
        if (sender instanceof Player)
        {
            boolean isperm = hasPermOnServerInWorld(sender, perm);
            if (!isperm && msg)
            {
                Component t = Component.text(Lang.translate(Lang.MessageType.NO_PERM));
                sender.sendMessage(t);
            }
            return isperm;
        }
        else
        {
            Component t = Component.text(Lang.translate(Lang.MessageType.NO_PERM));
            sender.sendMessage(t);
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
    public boolean hasOrConsoleOnServerInWorld(CommandSource sender, String perm, boolean msg)
    {
        boolean isperm = hasPermOnServerInWorld(sender, perm) || new VelocitySender(sender).isConsole();
        if (!isperm && msg)
        {
            Component t = Component.text(Lang.translate(Lang.MessageType.NO_PERM));
            sender.sendMessage(t);
        }
        return isperm;
    }
}
