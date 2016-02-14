package net.alpenblock.bungeeperms.platform.bukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BungeePermsReloadedEvent extends Event
{

    public static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

}
