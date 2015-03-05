package net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.worldedit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.lang.reflect.Field;
import java.util.List;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin;
import net.alpenblock.bungeeperms.platform.bukkit.bridge.Bridge;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class WorldEditBridge implements Bridge
{

    @Override
    public void enable()
    {
        Bukkit.getPluginManager().registerEvents(this, BukkitPlugin.getInstance());
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (plugin != null)
        {
            inject(plugin);
        }
    }

    @Override
    public void disable()
    {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
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
        if (!e.getPlugin().getName().equalsIgnoreCase("worldedit"))
        {
            return;
        }
        inject(e.getPlugin());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e)
    {
        if (!e.getPlugin().getName().equalsIgnoreCase("worldedit"))
        {
            return;
        }
        uninject(e.getPlugin());
    }

    public void inject(Plugin plugin)
    {
        BungeePerms.getLogger().info("Injection of BungeepermsBukkit into WorldEdit");
        try
        {
            WorldEditPlugin we = (WorldEditPlugin) plugin;
            
            if(!we.isEnabled())
            {
                return;
            }

            //inject BungeePerms
            Field f = we.getPermissionsResolver().getClass().getDeclaredField("enabledResolvers");
            f.setAccessible(true);
            ((List) f.get(we.getPermissionsResolver())).add(BungeePermsBukkitResolver.class);

            we.getPermissionsResolver().findResolver();
        }
        catch (Exception ex)
        {
        }
    }

    public void uninject(Plugin plugin)
    {
        BungeePerms.getLogger().info("Uninjection of BungeepermsBukkit into WorldEdit");
        try
        {
            WorldEditPlugin we = (WorldEditPlugin) plugin;

            //inject BungeePerms
            Field f = we.getPermissionsResolver().getClass().getDeclaredField("enabledResolvers");
            f.setAccessible(true);
            ((List) f.get(we.getPermissionsResolver())).remove(BungeePermsBukkitResolver.class);

            we.getPermissionsResolver().findResolver();
        }
        catch (Exception ex)
        {
        }
    }
}
