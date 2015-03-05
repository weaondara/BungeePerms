package net.alpenblock.bungeeperms.platform.bukkit;

import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.PermissionsPostProcessor;
import net.alpenblock.bungeeperms.platform.Sender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;

public class SuperPermsPostProcessor implements PermissionsPostProcessor
{

    @Override
    public Boolean process(String perm, Boolean result, Sender s)
    {
        //result found? sender not found?
        if (result != null || s == null)
        {
            return result;
        }

        if (((BukkitConfig) BungeePerms.getInstance().getConfig()).isSuperpermscompat())
        {
            Player p = BungeePerms.getInstance().getConfig().isUseUUIDs() ? Bukkit.getServer().getPlayer(s.getUUID()) : Bukkit.getServer().getPlayer(s.getName());
            if (p != null)
            {
                PermissibleBase base = Injector.getPermissible(p);
                if (base instanceof Permissible)
                {
                    Permissible permissible = (Permissible) base;
                    result = permissible.hasSuperPerm(perm);
                }
            }
        }

        return result;
    }
}
