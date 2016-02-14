package net.alpenblock.bungeeperms.platform.bukkit.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.alpenblock.bungeeperms.Group;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@AllArgsConstructor
public class BungeePermsGroupChangedEvent extends Event
{

    public static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    @Getter
    private final Group group;

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

}
