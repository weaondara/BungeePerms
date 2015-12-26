package net.alpenblock.bungeeperms.platform.bukkit;

import lombok.SneakyThrows;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.EventDispatcher;
import net.alpenblock.bungeeperms.platform.bukkit.event.BungeePermsGroupChangedEvent;
import net.alpenblock.bungeeperms.platform.bukkit.event.BungeePermsReloadedEvent;
import net.alpenblock.bungeeperms.platform.bukkit.event.BungeePermsUserChangedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

public class BukkitEventDispatcher implements EventDispatcher
{

    @Override
    public void dispatchReloadedEvent()
    {
        callSyncEvent(BukkitPlugin.getInstance(), new BungeePermsReloadedEvent());
    }

    @Override
    public void dispatchGroupChangeEvent(Group g)
    {
        callSyncEvent(BukkitPlugin.getInstance(), new BungeePermsGroupChangedEvent(g));
    }

    @Override
    public void dispatchUserChangeEvent(User u)
    {
        callSyncEvent(BukkitPlugin.getInstance(), new BungeePermsUserChangedEvent(u));
    }
    
    @SneakyThrows
    private static void runSync(Plugin p, Runnable r, boolean waitfinished)
    {
        if (Bukkit.isPrimaryThread())
        {
            r.run();
        }
        else
        {
            int id = Bukkit.getScheduler().runTask(p, r).getTaskId();
            if (waitfinished)
            {
                while (Bukkit.getScheduler().isCurrentlyRunning(id) || Bukkit.getScheduler().isQueued(id))
                {
                    Thread.sleep(1);
                }
            }
        }
    }
    
    private static void callSyncEvent(Plugin p, final Event e)
    {
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                Bukkit.getPluginManager().callEvent(e);
            }
        };
        runSync(p, r, true);
    }
}
