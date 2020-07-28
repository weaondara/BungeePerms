package net.alpenblock.bungeeperms.platform.bungee;

import lombok.AllArgsConstructor;
import net.alpenblock.bungeeperms.platform.ScheduledTask;

@AllArgsConstructor
public class BungeeScheduledTask implements ScheduledTask
{

    private final net.md_5.bungee.api.scheduler.ScheduledTask task;

    @Override
    public void cancel()
    {
        task.cancel();
    }
}
