/* 
 * Copyright (C) 2020 wea_ondara
 *
 * BungeePerms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BungeePerms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
