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
            Permission p = Bukkit.getPluginManager().getPermission(perms.get(i));
            if(p == null || p.getChildren().isEmpty())
            {
                continue;
            }
            System.out.println(p.getChildren());
            for (Map.Entry<String, Boolean> e : p.getChildren().entrySet())
            {
                if (e.getValue())
                {
                    perms.add(++i, e.getKey());
                }
            }
        }

        return perms;
    }
}
