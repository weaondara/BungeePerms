package net.alpenblock.bungeeperms.platform.bukkit;

import java.util.List;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.PermissionsPreProcessor;
import net.alpenblock.bungeeperms.platform.Sender;

public class OpProcessor implements PermissionsPreProcessor
{

    @Override
    public List<String> process(List<String> perms, Sender s)
    {
        BukkitConfig config = (BukkitConfig) BungeePerms.getInstance().getConfig();

        //ops have all
        if (config.isAllowops() && s != null && s.isOperator())
        {
            perms.add("*");
        }
        return perms;
    }
}
