package net.alpenblock.bungeeperms.platform.bukkit;

import java.lang.reflect.Field;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;

public class Injector 
{
    public static org.bukkit.permissions.Permissible inject(CommandSender sender, org.bukkit.permissions.Permissible newpermissible) 
    {
        try
        {
            Field perm=getPermField(sender);
            if(perm==null)
            {
                return null;
            }
            perm.setAccessible(true);
            org.bukkit.permissions.Permissible oldpermissible = (org.bukkit.permissions.Permissible) perm.get(sender);
            if (newpermissible instanceof PermissibleBase) 
            {
                //copy attachments
                Field attachments = PermissibleBase.class.getDeclaredField("attachments");
                attachments.setAccessible(true);
                ((List) attachments.get(newpermissible)).addAll((List)attachments.get(oldpermissible));
            }

            // inject permissible
            perm.set(sender, newpermissible);
            return oldpermissible;
        }
        catch (Exception e) {e.printStackTrace();}
		return null;
	}
    public static org.bukkit.permissions.Permissible uninject(CommandSender sender) 
    {
        try
        {
            Field perm=getPermField(sender);
            if(perm==null)
            {
                return null;
            }
            perm.setAccessible(true);
            org.bukkit.permissions.Permissible permissible = (org.bukkit.permissions.Permissible) perm.get(sender);
            if (permissible instanceof Permissible) 
            {
                perm.set(sender, ((Permissible)permissible).getOldPermissible());
                return (Permissible)permissible;
            }

            return null;
        }
        catch (Exception e) {e.printStackTrace();}
		return null;
	}
    
    public static org.bukkit.permissions.PermissibleBase getPermissible(CommandSender sender) 
    {
        try
        {
            Field perm=getPermField(sender);
            if(perm==null)
            {
                return null;
            }
            perm.setAccessible(true);
            PermissibleBase permissible = (PermissibleBase) perm.get(sender);

            return permissible;
        }
        catch (Exception e) {e.printStackTrace();}
		return null;
    }
    
    private static Field getPermField(CommandSender sender) 
    {
        Field perm=null;
        try
        {
            if(sender instanceof Player)
            {
                perm = Class.forName(getVersionedClassName("entity.CraftHumanEntity")).getDeclaredField("perm");
            }
            else if(sender instanceof ConsoleCommandSender)
            {
                perm = Class.forName(getVersionedClassName("command.ServerCommandSender")).getDeclaredField("perm");
            }
        }
        catch (Exception e) {e.printStackTrace();}
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
