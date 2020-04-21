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
package net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.essentials;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.perm.IPermissionsHandler;
import com.earth2me.essentials.perm.PermissionsHandler;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin;
import net.alpenblock.bungeeperms.platform.bukkit.bridge.Bridge;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;

public class EssentialsBridge implements Bridge
{

    @Override
    public void enable()
    {
        Bukkit.getPluginManager().registerEvents(this, BukkitPlugin.getInstance());
        final Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (plugin != null && plugin.isEnabled())
        {
            Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    inject(plugin);
                }
            };
            Bukkit.getScheduler().runTask(BukkitPlugin.getInstance(), r);
        }
    }

    @Override
    public void disable()
    {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (plugin != null)
        {
            uninject(plugin);
        }

        PluginEnableEvent.getHandlerList().unregister(this);
        PluginDisableEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onPluginEnable(final PluginEnableEvent e)
    {
        if (!e.getPlugin().getName().equalsIgnoreCase("Essentials"))
        {
            return;
        }
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                inject(e.getPlugin());
            }
        };
        Bukkit.getScheduler().runTask(BukkitPlugin.getInstance(), r);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e)
    {
        if (!e.getPlugin().getName().equalsIgnoreCase("Essentials"))
        {
            return;
        }
        uninject(e.getPlugin());
    }

    @EventHandler
    public void onEssReloadCommandConsole(ServerCommandEvent e)
    {
        handleEssReload(e.getCommand());
    }

    @EventHandler
    public void onEssReloadCommandPlayer(PlayerCommandPreprocessEvent e)
    {
        handleEssReload(e.getMessage());
    }

    private void handleEssReload(String cmd)
    {
        if (cmd.startsWith("/"))
            cmd = cmd.substring(1);
        if (!cmd.startsWith("ess"))
            return;
        String[] split = cmd.split(" ");
        if (split.length <= 1 || !split[1].equalsIgnoreCase("reload"))
            return;

        // /ess reload executed
        final Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (!plugin.isEnabled())
            return;

        Bukkit.getScheduler().runTaskLater(BukkitPlugin.getInstance(), new Runnable()
                                   {
                                       @Override
                                       public void run()
                                       {
                                           inject(plugin);
                                       }
                                   }, 1);
    }

    public void inject(Plugin plugin)
    {
        BungeePerms.getLogger().info("Injection of Bungeeperms into Essentials");
        try
        {
            Essentials ess = (Essentials) plugin;

            if (!ess.isEnabled())
                return;

            //get ess permhandler
            Field f = ess.getClass().getDeclaredField("permissionsHandler");
            f.setAccessible(true);
            PermissionsHandler permhandler = (PermissionsHandler) f.get(plugin);

            //inject bungeeperms
            f = permhandler.getClass().getDeclaredField("handler");
            f.setAccessible(true);
            IPermissionsHandler handler = (IPermissionsHandler) f.get(permhandler);

            BungeePerms bpPlugin = BungeePerms.getInstance();
            if (bpPlugin != null && bpPlugin.isEnabled())
            {
                if (!(handler instanceof BungeePermsHandler))
                {
                    Logger.getLogger("Essentials").log(Level.INFO, "Essentials: Using BungeePerms based permissions.");
                    handler = new BungeePermsHandler();
                    f.set(permhandler, handler);
                }
            }
        }
        catch (Exception ex)
        {
            BungeePerms.getInstance().getDebug().log(ex);
        }
    }

    public void uninject(Plugin plugin)
    {
        BungeePerms.getLogger().info("Uninjection of Bungeeperms into Essentials");

        try
        {
            Essentials ess = (Essentials) plugin;

            Field f = ess.getClass().getDeclaredField("permissionsHandler");
            f.setAccessible(true);
            PermissionsHandler permhandler = (PermissionsHandler) f.get(plugin);

            permhandler.checkPermissions();
        }
        catch (Exception ex)
        {
            BungeePerms.getInstance().getDebug().log(ex);
        }
    }
}
