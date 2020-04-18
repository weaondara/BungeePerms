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
