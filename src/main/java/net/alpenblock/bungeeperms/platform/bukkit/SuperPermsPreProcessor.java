package net.alpenblock.bungeeperms.platform.bukkit;

import java.util.List;
import java.util.Map;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.PermissionsPreProcessor;
import net.alpenblock.bungeeperms.platform.Sender;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public class SuperPermsPreProcessor implements PermissionsPreProcessor
{

    @Override
    public List<String> process(List<String> perms, Sender s)
    {
        BukkitConfig config = (BukkitConfig) BungeePerms.getInstance().getConfig();
        if (!config.isSuperpermscompat())
        {
            return perms;
        }

        //expand permissions
        for (int i = 0; i < perms.size(); i++)
        {
            //add all child perms
            String perm = perms.get(i);
            boolean neg = perm.startsWith("-");
            perm = neg ? perm.substring(1) : perm;

            Permission p = Bukkit.getPluginManager().getPermission(perm);
            if (p == null || p.getChildren().isEmpty())
            {
                continue;
            }
            System.out.println(p.getChildren());
            for (Map.Entry<String, Boolean> e : p.getChildren().entrySet())
            {
                if (e.getValue())
                {
                    perms.add(++i, (neg ? "-" : "") + e.getKey());
                }
            }
        }

        return perms;
    }
}
