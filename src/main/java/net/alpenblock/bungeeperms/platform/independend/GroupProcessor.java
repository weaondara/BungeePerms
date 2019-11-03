package net.alpenblock.bungeeperms.platform.independend;

import java.util.List;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BPPermission;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.PermissionsManager;
import net.alpenblock.bungeeperms.PermissionsPreProcessor;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.Sender;

public class GroupProcessor implements PermissionsPreProcessor
{

    @Override
    public List<BPPermission> process(List<BPPermission> perms, Sender s)
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

            for (String g : u.getGroupsString())
            {
                perms.add(0, new BPPermission("group." + Statics.toLower(g), "GroupProcessor", true, null, null, null, null));
            }
        }

        return perms;
    }

}
