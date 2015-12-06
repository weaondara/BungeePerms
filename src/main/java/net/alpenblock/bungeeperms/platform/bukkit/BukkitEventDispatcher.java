package net.alpenblock.bungeeperms.platform.bukkit;

import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.EventDispatcher;
import net.alpenblock.bungeeperms.platform.bukkit.event.BungeePermsGroupChangedEvent;
import net.alpenblock.bungeeperms.platform.bukkit.event.BungeePermsReloadedEvent;
import net.alpenblock.bungeeperms.platform.bukkit.event.BungeePermsUserChangedEvent;
import org.bukkit.Bukkit;

public class BukkitEventDispatcher implements EventDispatcher
{

    @Override
    public void dispatchReloadedEvent()
    {
        Bukkit.getPluginManager().callEvent(new BungeePermsReloadedEvent());
    }

    @Override
    public void dispatchGroupChangeEvent(Group g)
    {
        Bukkit.getPluginManager().callEvent(new BungeePermsGroupChangedEvent(g));
    }

    @Override
    public void dispatchUserChangeEvent(User u)
    {
        Bukkit.getPluginManager().callEvent(new BungeePermsUserChangedEvent(u));
    }

}
