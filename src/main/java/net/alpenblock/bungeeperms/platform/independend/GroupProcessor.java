package net.alpenblock.bungeeperms.platform.independend;

import java.util.List;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.PermissionsManager;
import net.alpenblock.bungeeperms.PermissionsPreProcessor;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.Sender;

public class GroupProcessor implements PermissionsPreProcessor
{

    @Override
    public List<String> process(List<String> perms, Sender s)
    {
        if (s == null)
        {
            return perms;
        }
        BPConfig config = BungeePerms.getInstance().getConfig();
        if (config.isGroupPermission())
        {
            PermissionsManager pm = BungeePerms.getInstance().getPermissionsManager();
            User u = config.isUseUUIDs() ? pm.getUser(s.getUUID()) : pm.getUser(s.getName());
            if (u == null)
            {
                return perms;
            }

            for (Group g : u.getGroups())
            {
                perms.add(0, "group." + g.getName().toLowerCase());
            }
        }

        return perms;
    }

}
