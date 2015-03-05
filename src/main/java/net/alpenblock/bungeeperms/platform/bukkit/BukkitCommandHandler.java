package net.alpenblock.bungeeperms.platform.bukkit;

import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.CommandHandler;
import net.alpenblock.bungeeperms.PermissionsChecker;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.Sender;

public class BukkitCommandHandler extends CommandHandler
{

    public BukkitCommandHandler(PlatformPlugin plugin, PermissionsChecker checker, BPConfig config)
    {
        super(plugin, checker, config);
    }

    @Override
    public boolean onCommand(Sender sender, String cmd, String label, String[] args)
    {
        boolean b = super.onCommand(sender, cmd, label, args);
        if(b)
        {
            return b;
        }
        return BukkitPlugin.getInstance().getBridge().onCommand(sender, cmd, label, args);
    }
}
