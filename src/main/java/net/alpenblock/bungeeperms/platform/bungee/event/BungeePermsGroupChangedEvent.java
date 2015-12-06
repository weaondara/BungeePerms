package net.alpenblock.bungeeperms.platform.bungee.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.alpenblock.bungeeperms.Group;
import net.md_5.bungee.api.plugin.Event;

@AllArgsConstructor
public class BungeePermsGroupChangedEvent extends Event
{

    @Getter
    private final Group group;
}
