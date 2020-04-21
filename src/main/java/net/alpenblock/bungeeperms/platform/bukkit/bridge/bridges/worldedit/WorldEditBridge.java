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
        BungeePerms.getLogger().info("Injection of Bungeeperms into WorldEdit"); //todo even more lang support
        try
        {
            WorldEditPlugin we = (WorldEditPlugin) plugin;

            if (!we.isEnabled())
            {
                return;
            }

            //inject BungeePerms
            Field f = we.getPermissionsResolver().getClass().getDeclaredField("enabledResolvers");
            f.setAccessible(true);
            ((List) f.get(we.getPermissionsResolver())).add(BungeePermsResolver.class);

            we.getPermissionsResolver().findResolver();
        }
        catch (Exception ex)
        {
        }
    }

    public void uninject(Plugin plugin)
    {
        BungeePerms.getLogger().info("Uninjection of Bungeeperms into WorldEdit");
        try
        {
            WorldEditPlugin we = (WorldEditPlugin) plugin;

            //inject BungeePerms
            Field f = we.getPermissionsResolver().getClass().getDeclaredField("enabledResolvers");
            f.setAccessible(true);
            ((List) f.get(we.getPermissionsResolver())).remove(BungeePermsResolver.class);

            we.getPermissionsResolver().findResolver();
        }
        catch (Exception ex)//todo report error
        {
        }
    }
}
