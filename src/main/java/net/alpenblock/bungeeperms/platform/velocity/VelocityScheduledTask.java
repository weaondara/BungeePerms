package net.alpenblock.bungeeperms.platform.velocity;

import lombok.AllArgsConstructor;
import net.alpenblock.bungeeperms.platform.ScheduledTask;

@AllArgsConstructor
public class VelocityScheduledTask implements ScheduledTask
{

    private final com.velocitypowered.api.scheduler.ScheduledTask task;

    @Override
    public void cancel()
    {
        task.cancel();
    }
}
