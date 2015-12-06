package net.alpenblock.bungeeperms.platform.bukkit.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.alpenblock.bungeeperms.User;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@AllArgsConstructor
public class BungeePermsUserChangedEvent extends Event
{

    public static HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    @Getter
    private final User user;

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

}
