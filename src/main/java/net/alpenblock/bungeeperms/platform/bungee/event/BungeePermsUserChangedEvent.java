package net.alpenblock.bungeeperms.platform.bungee.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.alpenblock.bungeeperms.User;
import net.md_5.bungee.api.plugin.Event;

@AllArgsConstructor
public class BungeePermsUserChangedEvent extends Event
{

    @Getter
    private final User user;
}
