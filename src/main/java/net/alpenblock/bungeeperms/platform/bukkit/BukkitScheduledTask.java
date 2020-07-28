package net.alpenblock.bungeeperms.platform.bukkit;

import lombok.AllArgsConstructor;
import net.alpenblock.bungeeperms.platform.ScheduledTask;

@AllArgsConstructor
public class BukkitScheduledTask implements ScheduledTask
{

    private final org.bukkit.scheduler.BukkitTask task;

    @Override
    public void cancel()
    {
        task.cancel();
    }
}
