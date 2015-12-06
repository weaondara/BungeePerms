package net.alpenblock.bungeeperms.platform.bungee;

import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.EventDispatcher;
import net.alpenblock.bungeeperms.platform.bungee.event.BungeePermsGroupChangedEvent;
import net.alpenblock.bungeeperms.platform.bungee.event.BungeePermsReloadedEvent;
import net.alpenblock.bungeeperms.platform.bungee.event.BungeePermsUserChangedEvent;
import net.md_5.bungee.api.ProxyServer;

public class BungeeEventDispatcher implements EventDispatcher
{

    @Override
    public void dispatchReloadedEvent()
    {
        ProxyServer.getInstance().getPluginManager().callEvent(new BungeePermsReloadedEvent());
    }

    @Override
    public void dispatchGroupChangeEvent(Group g)
    {
        ProxyServer.getInstance().getPluginManager().callEvent(new BungeePermsGroupChangedEvent(g));
    }

    @Override
    public void dispatchUserChangeEvent(User u)
    {
        ProxyServer.getInstance().getPluginManager().callEvent(new BungeePermsUserChangedEvent(u));
    }

}
