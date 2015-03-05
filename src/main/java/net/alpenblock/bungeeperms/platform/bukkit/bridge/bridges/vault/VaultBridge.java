/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.vault;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin;
import net.alpenblock.bungeeperms.platform.bukkit.bridge.Bridge;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

public class VaultBridge implements Bridge
{

    @Override
    public void enable()
    {
        Bukkit.getPluginManager().registerEvents(this, BukkitPlugin.getInstance());
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (plugin != null)
        {
            inject(plugin);
        }
    }

    @Override
    public void disable()
    {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (plugin != null)
        {
            uninject(plugin);
        }

        PluginEnableEvent.getHandlerList().unregister(this);
        PluginDisableEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e)
    {
        if (!e.getPlugin().getName().equalsIgnoreCase("vault"))
        {
            return;
        }
        inject(e.getPlugin());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e)
    {
        if (!e.getPlugin().getName().equalsIgnoreCase("vault"))
        {
            return;
        }
        uninject(e.getPlugin());
    }

    public void inject(Plugin plugin)
    {
        BungeePerms.getLogger().info("Injection of BungeepermsBukkit into Vault");
        try
        {
            Vault v = (Vault) plugin;
            
            if(!v.isEnabled())
            {
                return;
            }

            //inject BungeePerms permissions
            Method m = v.getClass().getDeclaredMethod("hookPermission", String.class, Class.class, ServicePriority.class, String[].class);
            m.setAccessible(true);
            m.invoke(v, "BungeePermsBukkit", Permission_BungeePermsBukkit.class, ServicePriority.Normal, new String[]
             {
                 "net.alpenblock.bungeeperms.bukkit.BungeePerms"
            });

            Field f = v.getClass().getDeclaredField("perms");
            f.setAccessible(true);
            f.set(v, Bukkit.getServicesManager().getRegistration(Permission.class).getProvider());

            //inject BungeePerms chat
            m = v.getClass().getDeclaredMethod("hookChat", String.class, Class.class, ServicePriority.class, String[].class);
            m.setAccessible(true);
            m.invoke(v, "BungeePermsBukkit", Chat_BungeePermsBukkit.class, ServicePriority.Normal, new String[]
             {
                 "net.alpenblock.bungeeperms.bukkit.BungeePerms"
            });

            //not needed
//            f = v.getClass().getDeclaredField("chat");
//            f.setAccessible(true);
//            f.set(v, Bukkit.getServicesManager().getRegistration(Permission.class).getProvider());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void uninject(Plugin plugin)
    {
        BungeePerms.getLogger().info("Uninjection of BungeepermsBukkit into Vault");
        
        try
        {
            Vault v = (Vault) plugin;
            
            if(!v.isEnabled())
            {
                return;
            }
            
            //uninject BungeePerms permissions
            Method m = v.getClass().getDeclaredMethod("loadChat");
            m.setAccessible(true);
            m.invoke(v);

            //inject BungeePerms chat
            m = v.getClass().getDeclaredMethod("loadPermission");
            m.setAccessible(true);
            m.invoke(v);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
