package net.alpenblock.bungeeperms.platform.bukkit;

import java.lang.reflect.Field;
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
