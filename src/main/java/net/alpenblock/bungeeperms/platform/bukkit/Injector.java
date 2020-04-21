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
package net.alpenblock.bungeeperms.platform.bukkit;

import java.lang.reflect.Field;
import net.alpenblock.bungeeperms.BungeePerms;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class Injector
{

    public static void inject(CommandSender sender, Permissible newpermissible)
    {
        try
        {
            Field perm = getPermField(sender);
            if (perm == null)
            {
                return;
            }
            perm.setAccessible(true);
            perm.set(sender, newpermissible);
        }
        catch (Exception e)
        {
            BungeePerms.getInstance().getDebug().log(e);
        }
    }

    public static void uninject(CommandSender sender)
    {
        Permissible perm = getPermissible(sender);
        if (perm instanceof BPPermissible)
        {
            net.alpenblock.bungeeperms.platform.bukkit.BPPermissible p = (net.alpenblock.bungeeperms.platform.bukkit.BPPermissible) perm;
            p.uninject();
        }
    }

    public static Permissible getPermissible(CommandSender sender)
    {
        try
        {
            Field perm = getPermField(sender);
            if (perm == null)
            {
                return null;
            }
            perm.setAccessible(true);
            Permissible permissible = (Permissible) perm.get(sender);

            return permissible;
        }
        catch (Exception e)
        {
            BungeePerms.getInstance().getDebug().log(e);
        }
        return null;
    }

    private static Field getPermField(CommandSender sender)
    {
        Field perm = null;
        try
        {
            if (sender instanceof Player)
            {
                perm = Class.forName(getVersionedClassName("entity.CraftHumanEntity")).getDeclaredField("perm");
            }
            else if (sender instanceof ConsoleCommandSender)
            {
                perm = Class.forName(getVersionedClassName("command.ServerCommandSender")).getDeclaredField("perm");
            }
        }
        catch (Exception e)
        {
            BungeePerms.getInstance().getDebug().log(e);
        }
        return perm;
    }

    private static String getVersionedClassName(String classname)
    {
        String version;

        Class serverClass = Bukkit.getServer().getClass();
        if (!serverClass.getSimpleName().equals("CraftServer"))
        {
            return null;
        }
        else if (serverClass.getName().equals("org.bukkit.craftbukkit.CraftServer"))
        {
            version = ".";
        }
        else
        {
            version = serverClass.getName().substring("org.bukkit.craftbukkit".length());
            version = version.substring(0, version.length() - "CraftServer".length());
        }

        return "org.bukkit.craftbukkit" + version + classname;
    }
}
