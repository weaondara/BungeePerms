package net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.placeholderapi;

import net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin;
import net.alpenblock.bungeeperms.platform.bukkit.bridge.Bridge;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class PlaceholderApiBridge implements Bridge
{
    PlaceholderProvider provider;
    
    @Override
    public void enable()
    {
        Bukkit.getPluginManager().registerEvents(this, BukkitPlugin.getInstance());
        final Plugin plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (plugin != null && plugin.isEnabled())
        {
            Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    provider = new PlaceholderProvider();
                    provider.register();
                }
            };
            Bukkit.getScheduler().runTask(BukkitPlugin.getInstance(), r);
        }
    }

    @Override
    public void disable()
    {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (plugin != null)
        {
 //           provider.unregister();
        }

        PluginEnableEvent.getHandlerList().unregister(this);
        PluginDisableEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onPluginEnable(final PluginEnableEvent e)
    {
        if (!e.getPlugin().getName().equalsIgnoreCase("PlaceholderAPI"))
        {
            return;
        }
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                provider = new PlaceholderProvider();
                provider.register();
            }
        };
        Bukkit.getScheduler().runTask(BukkitPlugin.getInstance(), r);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e)
    {
        if (!e.getPlugin().getName().equalsIgnoreCase("PlaceholderAPI"))
        {
            return;
        }
  //           provider.unregister();       
    }
}
